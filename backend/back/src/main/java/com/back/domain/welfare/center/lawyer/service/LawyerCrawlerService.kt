package com.back.domain.welfare.center.lawyer.service

import com.back.domain.welfare.center.lawyer.entity.Lawyer
import org.apache.http.conn.ConnectTimeoutException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.io.IOException
import java.net.SocketTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

@Service
class LawyerCrawlerService(
    private val lawyerSaveService: LawyerSaveService
) {
    @Autowired
    @Lazy
    private lateinit var self: LawyerCrawlerService
    // crawlMultiPages 가 crawlEachPage를 호출할 때 crawlEachPage의 @Retryable이 작동하도록 하기 위함
    // this.crawlEachPage() 쓰면 내부 호출이라 프록시 X -> @Retryable 작동 X
    // self.crawlEachPage() 써서 프록시 객체를 다시 주입받아서 호출 ->  에러 났을 때 @Retryable 작동 O

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val BATCH_SIZE = 100
        private const val SEARCH_URL = "https://www.youthlabor.co.kr/company/search?text=&area=%s&area2=&page=%d"
        private val regionList = listOf(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
        )
    }

    fun crawlAllPages() {
        // reginList의 모든 지역 순회하며 크롤링
        regionList.forEach { area1 ->
            val lastPage = getLastPage(area1)
            crawlMultiPages(area1, 1, lastPage)
        } // 크롤링 테스트 용이성을 위해서 crawlMultiPages. getLastPage 따로 분리했음.
    }


    fun crawlMultiPages(area1: String, startPage: Int, endPage: Int) {
        val lawyerList = mutableListOf<Lawyer>()

        (startPage..endPage).forEach { page ->
            val pageLawyers = self.crawlEachPage(area1, page)
            lawyerList.addAll(pageLawyers)

            // 설정한 배치 사이즈 기준마다 저장
            if (lawyerList.size >= BATCH_SIZE) {
                lawyerSaveService.saveList(lawyerList)
                lawyerList.clear()
            }
            applyDelay(300)
        }
        // 마지막 남은 데이터 저장
        if (lawyerList.isNotEmpty()) {
            lawyerSaveService.saveList(lawyerList)
            lawyerList.clear()
        }
    }

    @Retryable(
        // 어떤 에러가 발생했을 때 재시도할 것인가
        retryFor = [
            SocketTimeoutException::class,
            ResourceAccessException::class,
            ConnectTimeoutException::class,
            HttpClientErrorException.TooManyRequests::class,
            HttpServerErrorException.ServiceUnavailable::class
                   ],
        maxAttempts = 5, // 재시도 횟수 설정
        backoff = Backoff(delay = 2000, multiplier = 2.0)
        // 재시도 간격 설정(2000ms 로 시작 -> 대기 시간 2배씩 증가)
    )
    fun crawlEachPage(area1: String, startPage: Int): List<Lawyer> {
        val url = SEARCH_URL.format(area1, startPage)
        val rows = crawlAndSelectRows(url)

        return rows.mapNotNull { row ->
            val lawyer = parseRowToLawyer(row, area1)

            // 법무법인이 없는 노무사는 어차피 연락할 수단도 없기 때문에 제거
            if (lawyer?.corporation.isNullOrEmpty()) null else lawyer
        }
    }

    // 해당 열의 요소를 분해해서 데이터를 Lawyer로 저장
    private fun parseRowToLawyer(row: Element, area1: String): Lawyer? {
        val cols = row.select("td")

        if (cols.size < 3) return null

        val name = cols[1].text()
        val corporation = cols[2].text()
        val area2 = cols[0].text()

        return Lawyer(
            id = "${name}_${corporation}_${area1}_${area2}", // String.format 대신 문자열 템플릿($) 사용
            name = name,
            corporation = corporation,
            districtArea1 = area1,
            districtArea2 = area2
        )
    }

    // 딜레이 주는 기능 메서드로 따로 분리
    private fun applyDelay(millis: Int) {
        try {
            Thread.sleep(millis.toLong())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    fun crawlAndSelectRows(url: String): Elements {
        val doc =
            Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get()
        return doc.select("table tbody tr")
    // table의 -> tbody -> tr 조회
    }

    // 특정 지역의 마지막 페이지 숫자 받아오기
    fun getLastPage(area1: String): Int {
        return try {
            val url = SEARCH_URL.format(area1, 1)
            val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
            val href = doc.select("a.last").first()?.attr("href") ?: return 1
            // 해당 사이트의 HTML의 링크 태그 찾아서, 마지막 페이지 링크 뽑아옴

            href.substringAfterLast("=").toIntOrNull() ?: 1
            // url은 https://...&area2=&text=&page=(페이지 숫자) 의 구조로 이루어져 있으니,
            // '=' 기준으로 탐색 -> 그 뒤에 있는 문자열만 잘라와 마지막 페이지 숫자 뽑아옴

        } catch (e: IOException) {
            log.error("마지막 페이지 조회 에러 발생" , e)
            1
        }
    }
}

