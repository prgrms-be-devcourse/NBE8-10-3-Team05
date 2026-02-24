package com.back.domain.welfare.policy.service;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.policy.config.YouthPolicyProperties;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyApiClient {

    private final YouthPolicyProperties properties;
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                            )
                    .build())
            .build();
    private final ObjectMapper objectMapper; // Bean 주입

    /**
     * API에서 한 페이지 가져오기
     */
    public PolicyFetchResponseDto fetchPolicyPage(PolicyFetchRequestDto requestDto, int pageNum, int pageSize) {
        try {
            String requestUrl = properties.url()
                    + "?apiKeyNm=" + properties.key()
                    + "&pageType=" + requestDto.getPageType()
                    + "&pageSize=" + pageSize
                    + "&pageNum=" + pageNum
                    + "&rtnType=" + requestDto.getRtnType();

            String response = webClient
                    .get()
                    .uri(URI.create(requestUrl))
                    .retrieve()
                    .bodyToMono(String.class)
                    // delay넣지 않으면 외부 api에서 거부
                    // .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                    //       .filter(throwable -> throwable instanceof WebClientResponseException.InternalServerError))
                    // block으로 동기 처리
                    .block(Duration.ofSeconds(10)); // 10초 타임아웃

            if (response == null) {
                throw new RuntimeException("Policy API 응답이 null입니다.");
            }

            // JSON → DTO
            return objectMapper.readValue(response, PolicyFetchResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Policy API 호출 실패", e);
        }
    }
}
