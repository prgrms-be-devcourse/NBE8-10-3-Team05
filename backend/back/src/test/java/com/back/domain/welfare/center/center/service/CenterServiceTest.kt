package com.back.domain.welfare.center.center.service

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.center.repository.CenterRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
internal class CenterServiceTest {
    @Autowired
    private lateinit var centerService: CenterService

    @Autowired
    private lateinit var centerRepository: CenterRepository

    @MockitoBean
    private lateinit var centerApiService: CenterApiService

    @Test
    @DisplayName("getCenterData 테스트")
    fun t1() {
        val totalCnt = 9
        val responseDto = createResponseData(1, totalCnt, 10, totalCnt)

        `when`(centerApiService.fetchCenter(any())).thenReturn(responseDto)

        val centerList: MutableList<Center> = centerService.getCenterData()

        val savedCount = centerRepository.count()

        assertThat(centerList).isNotEmpty
        assertThat(centerList.size).isEqualTo(totalCnt)
        assertThat(savedCount).isEqualTo(totalCnt.toLong())
    }

    @Test
    @DisplayName("getCenterData 테스트 : 200개 이상 받을 때")
    fun t2() {
        val totalCnt = 250
        val responseDto = createResponseData(1, totalCnt, 10, totalCnt)

        `when`(centerApiService.fetchCenter(any())).thenReturn(responseDto)

        // when
        val centerList = centerService.getCenterData()

        // then
        assertThat(centerList).isNotEmpty
        assertThat(centerRepository.count()).isEqualTo(centerList.size.toLong())
    }

    private fun createResponseData(page: Int, perPage: Int, totalCount: Int, currentCount: Int): CenterApiResponseDto {
        // 4. 코틀린의 List 생성 방식 사용
        val mockList = (1..currentCount).map { i ->
            CenterDto(
                id = i + ((page - 1) * perPage),
                city = "강원",
                facilityName = "복지관$i",
                address = "주소",
                phoneNumber = "010-0000-0000",
                operator = "운영주체",
                corporationType = "법인"
            )
        }
        return CenterApiResponseDto(page, perPage, totalCount, currentCount, totalCount, mockList)
    }
}
