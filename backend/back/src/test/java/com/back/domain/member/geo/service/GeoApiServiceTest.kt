package com.back.domain.member.geo.service

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.dto.GeoApiResponseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled
class GeoApiServiceTest {

    @Autowired
    private lateinit var geoApiService: GeoApiService

    @Test
    @DisplayName("실제 카카오 local API 테스트")
    fun t0() {
        // Given
        val addressDto = AddressDto.builder()
            .roadAddress("경기도 성남시 분당구 분당로 50")
            .build()

        // When
        val result = geoApiService.fetchGeoCode(addressDto)

        // Then
        assertThat(result).isNotNull
        assertThat(result?.documents).isNotNull
        assertThat(result?.documents).isNotEmpty

        // 첫 번째 요소의 address 검증
        val firstDocument = result?.documents?.firstOrNull()
        assertThat(firstDocument?.address).isNotNull

        val resultAddress = firstDocument?.address
        assertThat(resultAddress?.hCode).isNotNull
        assertThat(resultAddress?.hCode).isNotEmpty
        assertThat(resultAddress?.x).isNotNull
        assertThat(resultAddress?.y).isNotNull
    }
}
