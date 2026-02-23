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

    // WebClient를 매번 생성하기보다 빈으로 주입받거나 한 번만 생성해서 재사용하는 것이 좋습니다.
    private val webClient: WebClient = WebClient.builder().build()

    fun fetchCenter(centerApiRequestDto: CenterApiRequestDto): CenterApiResponseDto {
        // 1. 문자열 템플릿 사용으로 가독성 향상
        val requestUrl = "${centerApiProperties.url}" +
            "?page=${centerApiRequestDto.page}" +
            "&perPage=${centerApiRequestDto.perPage}" +
            "&serviceKey=${centerApiProperties.key}"

        // 2. Optional 대신 코틀린의 Null 안정성 기능 활용
        return webClient
            .get()
            .uri(URI.create(requestUrl))
            .retrieve()
            .bodyToMono<CenterApiResponseDto>() // reified type parameter 활용
            .block()
            ?: throw ServiceException("501", "center api 데이터를 가져오는데 실패하였습니다.")
    }
}
