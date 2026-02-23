package com.back.domain.welfare.center.center.service

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto
import com.back.domain.welfare.center.center.properties.CenterApiProperties
import com.back.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class CenterApiService(private val centerApiProperties: CenterApiProperties) {

    private val webClient: WebClient = WebClient.builder().build()

    fun fetchCenter(centerApiRequestDto: CenterApiRequestDto): CenterApiResponseDto {
        val requestUrl = "${centerApiProperties.url}" +
            "?page=${centerApiRequestDto.page}" +
            "&perPage=${centerApiRequestDto.perPage}" +
            "&serviceKey=${centerApiProperties.key}"

        return webClient
            .get()
            .uri(URI.create(requestUrl))
            .retrieve()
            .bodyToMono<CenterApiResponseDto>()
            .block()
            ?: throw ServiceException("501", "center api 데이터를 가져오는데 실패하였습니다.")
    }
}
