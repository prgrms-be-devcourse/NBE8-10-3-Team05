package com.back.domain.welfare.estate.service

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto
import com.back.domain.welfare.estate.properties.EstateConfigProperties
import com.back.global.exception.ServiceException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI

@Service
class EstateApiClient(private val estateConfigProperties: EstateConfigProperties) {

    private val webClient: WebClient = WebClient.builder().build()
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API
     */
    fun fetchEstatePage(requestDto: EstateFetchRequestDto): EstateFetchResponseDto {
        val pageSize = requestDto.numOfRows ?: 10
        val pageNo = requestDto.pageNo ?: 1

        log.debug("fetchEstatePage requestDto: {}, pageSize : {}, pageNo : {}", requestDto, pageSize, pageNo)

        val requestUrl = "${estateConfigProperties.url}" +
            "?serviceKey=${estateConfigProperties.key}" +
            "&numOfRows=$pageSize" +
            "&pageNo=$pageNo"

        return webClient
            .get()
            .uri(URI.create(requestUrl))
            .retrieve()
            .bodyToMono<EstateFetchResponseDto>() // Reified Type 사용 (import 필요)
            .block()
            ?: throw ServiceException("501", "API 호출 과정에 에러가 생겼습니다.")
    }
}
