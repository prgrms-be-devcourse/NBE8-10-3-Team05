package com.back.domain.welfare.estate.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.properties.EstateConfigProperties;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstateApiClient {
    private final WebClient webClient = WebClient.builder().build();
    private final EstateConfigProperties estateConfigProperties;

    // 국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API
    public EstateFetchResponseDto fetchEstatePage(EstateFetchRequestDto requestDto) {
        int pageSize = requestDto.numOfRows;
        int pageNo = requestDto.pageNo;

        log.debug("fetchEstatePage requestDto: {}, pageSize : {}, pageNo : {}", requestDto, pageSize, pageNo);

        String requestUrl = estateConfigProperties.url()
                + "?serviceKey=" + estateConfigProperties.key()
                + "&numOfRows=" + pageSize
                + "&pageNo=" + pageNo;

        EstateFetchResponseDto responseDto = Optional.ofNullable(webClient
                        .get()
                        .uri(URI.create(requestUrl))
                        .retrieve()
                        .bodyToMono(EstateFetchResponseDto.class)
                        .block())
                .orElseThrow(() -> new ServiceException("501", "API호출과정에 에러가 생겼습니다."));

        return responseDto;
    }
}
