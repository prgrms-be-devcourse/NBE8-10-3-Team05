package com.back.domain.welfare.estate.service

import com.back.domain.welfare.estate.dto.EstateDto
import com.back.domain.welfare.estate.dto.EstateFetchRequestDto
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto
import com.back.domain.welfare.estate.repository.EstateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
internal class EstateServiceTest {

    @Autowired
    private lateinit var estateService: EstateService

    @MockitoBean
    private lateinit var estateApiClient: EstateApiClient

    @Autowired
    private lateinit var estateRepository: EstateRepository

    @Test
    @DisplayName("mockResponse 테스트")
    fun t0() {
        val responseDto = mockResponse(10, 9)

        assertThat(responseDto).isNotNull
        assertThat(responseDto.response?.body).isNotNull

        val body = responseDto.response!!.body!!
        assertThat(body.numOfRows).isNotBlank()
        assertThat(body.pageNo).isNotBlank()
        assertThat(body.totalCount).isNotBlank()
        assertThat(body.items).hasSize(9)
    }

    @Test
    @DisplayName("fetchEstatePage 테스트")
    fun t1() {
        // Given
        val mockRes = mockResponse(10, 9)
        given(estateApiClient.fetchEstatePage(any()))
            .willReturn(mockRes)

        val requestDto = EstateFetchRequestDto(numOfRows = 100, pageNo = 1)

        // When
        val responseDto = estateApiClient.fetchEstatePage(requestDto)

        // Then
        assertThat(responseDto?.response?.body).isNotNull
        val body = responseDto.response!!.body!!
        assertThat(body.items).isNotNull
    }

    @Test
    @DisplayName("saveEstateList 테스트")
    fun t2() {
        // Given
        val mockRes = mockResponse(10, 9)
        given(estateApiClient.fetchEstatePage(any()))
            .willReturn(mockRes)

        val responseDto = estateApiClient.fetchEstatePage(EstateFetchRequestDto(numOfRows = 10, pageNo = 1))

        // When
        val estateList = estateService.saveEstateList(responseDto)
        val savedCnt = estateRepository.count().toInt()

        // Then
        assertThat(savedCnt).isEqualTo(estateList.size)
    }

    @Test
    @DisplayName("fetchEstateList 테스트")
    fun t3() {
        // Given
        val mockRes = mockResponse(10, 9)
        given(estateApiClient.fetchEstatePage(any()))
            .willReturn(mockRes)

        // When
        val estateList = estateService.fetchAndSaveAllEstates() // 리팩토링된 메서드 명칭 반영
        val savedCount = estateRepository.count().toInt()

        // Then
        assertThat(estateList).hasSize(9)
        assertThat(savedCount).isEqualTo(9)
    }

    @Test
    @DisplayName("fetchEstateList 테스트 : return값이 200개일 때")
    fun t4() {
        // Given
        val mockRes1 = mockResponse(200, 100)
        val mockRes2 = mockResponse(200, 100)
        given(estateApiClient.fetchEstatePage(any()))
            .willReturn(mockRes1)
            .willReturn(mockRes2)

        // When
        val estateList = estateService.fetchAndSaveAllEstates()
        val savedCount = estateRepository.count().toInt()

        // Then
        verify(estateApiClient, times(2)).fetchEstatePage(any())
        assertThat(estateList).hasSize(200)
        assertThat(savedCount).isEqualTo(200)
    }

    private fun mockResponse(pageSize: Int, currCnt: Int): EstateFetchResponseDto {
        // Kotlin의 List 생성 방식을 사용하여 IntStream 대체
        val mockItems = List(currCnt) { EstateDto() }

        val body = EstateFetchResponseDto.Response.BodyDto(
            numOfRows = "100",
            pageNo = "1",
            totalCount = pageSize.toString(),
            items = mockItems
        )

        val header = EstateFetchResponseDto.Response.HeaderDto(
            resultCode = "00",
            resultMsg = "NORMAL SERVICE."
        )

        return EstateFetchResponseDto(
            response = EstateFetchResponseDto.Response(header, body)
        )
    }
}
