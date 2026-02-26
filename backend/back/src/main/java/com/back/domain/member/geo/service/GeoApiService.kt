package com.back.domain.member.geo.service

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.dto.GeoApiResponseDto
import com.back.domain.member.geo.properties.GeoApiProperties
import com.back.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import java.util.function.Supplier
import org.springframework.web.reactive.function.client.bodyToMono


@Service
class GeoApiService(private val geoApiProperties: GeoApiProperties) {
    private val webClient: WebClient = WebClient.builder().build()

    // 카카오 Local API
    fun fetchGeoCode(addressDto: AddressDto): GeoApiResponseDto? {
        val requestUrl = geoApiProperties.url + "?query=" + addressDto.roadAddress

        val responseDto = Optional.ofNullable<GeoApiResponseDto?>(
            webClient
                .get()
                .uri(requestUrl)
                .header("Authorization", "KakaoAK " + geoApiProperties.key)
                .retrieve()
                .bodyToMono<GeoApiResponseDto>()
                .block()
        )
            .orElseThrow(Supplier { ServiceException("501", "kakao geo api 오류가 생겼습니다.") })

        return responseDto
    }
}
