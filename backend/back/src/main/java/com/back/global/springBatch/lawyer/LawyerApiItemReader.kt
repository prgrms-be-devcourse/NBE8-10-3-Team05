package com.back.global.springBatch.lawyer

import com.back.domain.welfare.center.lawyer.entity.Lawyer
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService
import org.hibernate.query.Page.page
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import java.util.concurrent.CopyOnWriteArrayList

@Component
@StepScope
class LawyerApiItemReader(
    private val lawyerCrawlerService: LawyerCrawlerService,
    @Value("#{stepExecutionContext['region']}")
    private val region: String
) : AbstractPagingItemReader<Lawyer>() {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        // 한번에 읽을 양 설정
        pageSize = 8
    }

    override fun doReadPage() {
        // 1. 결과 저장소 초기화 및 안전한 참조 생성 (Thread-safety)
        val currentResults = results ?: CopyOnWriteArrayList<Lawyer>().also { results = it }
        currentResults.clear()

        try {
            // 2. 데이터 크롤링 수행
            val data = lawyerCrawlerService.crawlEachPage(region, page + 1)

            // 3. 결과 추가 (null 방어)
            data?.let {
                currentResults.addAll(it.filterNotNull())
            }

        } catch (e: ResourceAccessException) {
            log.error(">>> [네트워크 타임아웃/연결오류] 지역: {}, 페이지: {}, 사유: {}", region, page, e.message)
            throw e
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn(">>> [차단 감지] 너무 많은 요청을 보냈습니다. (429 Too Many Requests)")
            }
            log.error(">>> [HTTP 에러] 상태코드: {}, 내용: {}", e.statusCode, e.responseBodyAsString)
            throw e
        } catch (e: RuntimeException) {
            log.error(">>> [데이터 처리 오류] API 결과 해석 중 에러 발생: {}", e.message)
            throw e
        } catch (e: Exception) {
            log.error(">>> [알 수 없는 치명적 에러] 지역: {}, 에러: {}", region, e.javaClass.simpleName)
            throw e
        }
    }
}
