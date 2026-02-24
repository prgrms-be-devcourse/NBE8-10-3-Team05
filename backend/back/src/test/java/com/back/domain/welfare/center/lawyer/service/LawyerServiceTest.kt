package com.back.domain.welfare.center.lawyer.service

import com.back.domain.welfare.center.lawyer.dto.LawyerReq
import com.back.domain.welfare.center.lawyer.dto.LawyerRes
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService
import com.back.domain.welfare.center.lawyer.service.LawyerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.function.Predicate

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class LawyerServiceTest {
    @Autowired
    private val lawyerCrawlerService: LawyerCrawlerService? = null

    @Autowired
    private val lawyerService: LawyerService? = null

    @Test
    @DisplayName("제주도의 서귀포시가 관할 구역인 노무사 검색 - 페이징 및 특정 인물 확인")
    @Throws(Exception::class)
    fun t1() {
        lawyerCrawlerService!!.crawlMultiPages("제주", 1, 1)

        val req = LawyerReq("제주특별자치도", "서귀포")
        val pageable: Pageable = PageRequest.of(0, 30)

        val resultPage: Page<LawyerRes> = lawyerService!!.searchByDistrict(req, pageable)
        val content = resultPage.getContent()

        Assertions.assertThat<LawyerRes?>(content).isNotEmpty()
        Assertions.assertThat<LawyerRes?>(content)
            .allMatch(Predicate { lawyerRes: LawyerRes? ->
                lawyerRes!!.districtArea1 == "제주"
                        && lawyerRes.districtArea2!!.contains("서귀포")
            })
        Assertions.assertThat(content.stream().anyMatch { lawyer: LawyerRes? -> lawyer!!.name == "강주리" })
            .isTrue()
    }

    @Test
    @DisplayName("충청남도의 공주시 노무사 검색 - 페이징 및 특정 인물 확인")
    @Throws(Exception::class)
    fun t2() {
        lawyerCrawlerService!!.crawlMultiPages("충남", 1, 1)

        val req = LawyerReq("충청남도", "공주")
        val pageable: Pageable = PageRequest.of(0, 30)

        val resultPage: Page<LawyerRes> = lawyerService!!.searchByDistrict(req, pageable)
        val content = resultPage.getContent()

        Assertions.assertThat<LawyerRes?>(content).isNotEmpty()
        Assertions.assertThat<LawyerRes?>(content)
            .allMatch(Predicate { lawyerRes: LawyerRes? ->
                lawyerRes!!.districtArea1 == "충남"
                        && lawyerRes.districtArea2!!.contains("공주")
            })
        Assertions.assertThat(content.stream().anyMatch { lawyer: LawyerRes? -> lawyer!!.name == "우재식" })
            .isTrue()

        println("검색된 공주 노무사 총 수: " + resultPage.getTotalElements())
    }
}
