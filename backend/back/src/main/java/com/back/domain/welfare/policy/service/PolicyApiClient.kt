package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.config.YouthPolicyProperties
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Component
class PolicyApiClient(
    private val properties: YouthPolicyProperties,
    private val objectMapper: ObjectMapper
) {

    private val webClient: WebClient = WebClient.builder()
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs { configurer: ClientCodecConfigurer ->
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                }
                .build()
        )
        .build()

    /**
     * API에서 한 페이지 가져오기
     */
    fun fetchPolicyPage(
        requestDto: PolicyFetchRequestDto,
        pageNum: Int,
        pageSize: Int
    ): PolicyFetchResponseDto {

        val uri = UriComponentsBuilder
            .fromUriString(properties.url)
            .queryParam("apiKeyNm", properties.key)
            .queryParam("pageType", requestDto.pageType)
            .queryParam("pageSize", pageSize)
            .queryParam("pageNum", pageNum)
            .queryParam("rtnType", requestDto.rtnType)
            .build(true)
            .toUri()

        val responseBody = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(String::class.java)
            .block(Duration.ofSeconds(10))
            ?: throw IllegalStateException("Policy API 응답이 null입니다.")

        return try {
            objectMapper.readValue(responseBody, PolicyFetchResponseDto::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Policy API 응답 파싱 실패", e)
        }
    }
}