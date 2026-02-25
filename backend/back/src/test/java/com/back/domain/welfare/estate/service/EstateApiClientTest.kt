package com.back.domain.welfare.estate.service

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Disabled
internal class EstateApiClientTest {

    @Autowired
    private lateinit var estateApiClient: EstateApiClient

    @Test
    @DisplayName("실제 API 테스트 : 필요할때만 수동으로 실행")
    fun fetchEstateAPI_real() {
        // Given: 빌더 대신 Kotlin의 Named Arguments를 사용하여 생성 가능 (DTO 리팩토링 덕분)
        val requestDto = EstateFetchRequestDto(
            numOfRows = 10,
            pageNo = 1
        )

        // When
        val responseDto = estateApiClient.fetchEstatePage(requestDto)

        // Then: AssertJ를 사용하면 문장이 더 자연스럽게 읽힙니다.
        assertThat(responseDto).withFailMessage("ResponseDto가 null입니다.").isNotNull

        val body = responseDto.response?.body
        assertThat(body).withFailMessage("ResponseDto.response.body가 null입니다.").isNotNull

        // Body 내부 필드 검증 (Safe Call 활용)
        assertThat(body?.numOfRows).withFailMessage("numOfRows가 비어있습니다.").isNotBlank()
        assertThat(body?.pageNo).withFailMessage("pageNo가 비어있습니다.").isNotBlank()
        assertThat(body?.totalCount).withFailMessage("totalCount가 비어있습니다.").isNotBlank()
    }
}
