package com.back.domain.welfare.center.lawyer.service

import com.back.domain.welfare.center.lawyer.repository.LawyerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.ArgumentMatchers.anyString
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.web.client.ResourceAccessException

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class LawyerCrawlerServiceTest {
    @MockitoSpyBean
    private lateinit var lawyerCrawlerService: LawyerCrawlerService

    @Autowired
    private lateinit var lawyerRepository: LawyerRepository

    @Test
    @DisplayName("서울 1,2페이지 크롤링 테스트")
    fun t1() {
        val initialCount = lawyerRepository.count()

        lawyerCrawlerService.crawlMultiPages("서울", 1, 2)

        val savedLawyers = lawyerRepository.findAll()
        assertThat(savedLawyers.size).isGreaterThanOrEqualTo(initialCount.toInt())
        assertThat(savedLawyers).isNotEmpty
        assertThat(savedLawyers[0].name).isNotBlank()
    }

    @Test
    @DisplayName("서울 첫 페이지 노무사 정보 확인")
    fun t2() {
        lawyerCrawlerService.crawlMultiPages("서울", 1, 1)

        val result = lawyerRepository.findAll()

        assertThat(result).isNotEmpty
        val targetLawyer = result.firstOrNull { it.name == "조갑식" }
            ?: throw NoSuchElementException("해당 노무사를 찾을 수 없습니다.")

        assertThat(targetLawyer.districtArea1).isEqualTo("서울")
        assertThat(targetLawyer.districtArea2).containsAnyOf("중구", "종로구", "서초구", "동대문구")
        assertThat(targetLawyer.corporation).isEqualTo("노무법인 케이에스")
    }

    @Test
    @DisplayName("서울 지역의 마지막 페이지 번호 조회 확인")
    fun t3() {
        val lastPage = lawyerCrawlerService.getLastPage("서울")

        assertThat(lastPage).isGreaterThan(0)
        assertThat(lastPage).isGreaterThanOrEqualTo(311)
        println("서울 마지막 페이지: $lastPage")
    }

    @Test
    @DisplayName("@Retryable검증-타임아웃 5번 발생 시 최종적으로 예외 터져야 함")
    fun t4() {
        doThrow(ResourceAccessException("강제 타임아웃 에러"))
            .`when`(lawyerCrawlerService).crawlAndSelectRows(anyString())

        assertThrows<ResourceAccessException> {
            lawyerCrawlerService.crawlEachPage("서울", 1)
        }

        verify(lawyerCrawlerService, times(5)).crawlAndSelectRows(anyString())
    }
}
