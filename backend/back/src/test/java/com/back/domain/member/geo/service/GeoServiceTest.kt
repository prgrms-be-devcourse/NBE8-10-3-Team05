package com.back.domain.member.geo.service

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.dto.GeoApiResponseDto
import com.back.domain.member.geo.dto.GeoApiResponseDto.RoadAddress
import com.back.domain.member.geo.entity.Address
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.List

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
internal class GeoServiceTest {

    @MockitoBean
    private lateinit var geoApiService: GeoApiService

    @Autowired
    private lateinit var geoService: GeoService

    @Test
    @DisplayName("getGeoCode 테스트")
    fun t1() {
        // given
        val addressDto = AddressDto.builder().addressName("경기도 성남시 분당구").build()
        val responseDto = mockResponse()

        // Mockito의 `when`을 사용할 때 Kotlin 예약어와 겹치므로 backtick(` `) 사용
        `when`(geoApiService.fetchGeoCode(any()))
            .thenReturn(responseDto)

        // when
        val result = geoService.getGeoCode(addressDto)

        // then
        assertThat(result).isNotNull
        assertThat(result.hCode).isEqualTo("4113500000")
        assertThat(result.latitude).isEqualTo(37.3947)
        assertThat(result.longitude).isEqualTo(127.1111)

        // repository의 search 메서드가 정확히 1번 호출되었는지 검증
        verify(geoApiService, times(1)).fetchGeoCode(addressDto)
    }

    private fun mockResponse(): GeoApiResponseDto {
        val address = GeoApiResponseDto.Address(
            addressName = "경기도 성남시 분당구 분당로 50",
            region1depthName = "경기",
            region2depthName = "성남시 분당구",
            region3depthName = "수내동",
            hCode = "4113500000",  // hCode (행정동 코드)
            bCode = "4113510300",  // bCode (법정동 코드)
            x = "127.1111",         // x (경도)
            y = "37.3947"           // y (위도)
        )

        val roadAddress = GeoApiResponseDto.RoadAddress(
            addressName = "경기도 성남시 분당구 분당로 50",
            zoneNo = "13590",
            x = "127.1111",
            y = "37.3947"
        )

        val document = GeoApiResponseDto.Document(
            addressName = "경기도 성남시 분당구 분당로 50",
            x = "127.1111",
            y = "37.3947",
            addressType = "REGION_ADDR",
            address = address,
            roadAddress = roadAddress
        )

        val meta = GeoApiResponseDto.Meta(1, 1, true)

        // Kotlin에서는 listOf()를 사용하여 더 간결하게 생성 가능합니다.
        return GeoApiResponseDto(meta, listOf(document))
    }
}
