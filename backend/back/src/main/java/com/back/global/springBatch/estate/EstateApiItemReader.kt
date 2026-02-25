package com.back.global.springBatch.estate

import com.back.domain.welfare.estate.dto.EstateDto
import com.back.domain.welfare.estate.dto.EstateFetchRequestDto
import com.back.domain.welfare.estate.service.EstateApiClient
import org.slf4j.LoggerFactory
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.util.concurrent.CopyOnWriteArrayList

@Component
class EstateApiItemReader(
    private val estateApiClient: EstateApiClient
) : AbstractPagingItemReader<EstateDto>() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val totalCount = 88
    private val apiKeys = listOf("1", "2", "3")
    private var currentKeyIdx = 0

    init {
        // 한번에 읽을 양 설정
        pageSize = 100
    }

    override fun doReadPage() {
        // 1. 결과 저장소 초기화 및 안전한 참조 생성
        val currentResults = results ?: CopyOnWriteArrayList<EstateDto>().also { results = it }
        currentResults.clear()

        // 2. 읽어야 할 데이터가 totalCount를 넘으면 종료
        if (page * pageSize >= totalCount) return

        var success = false
        while (!success && currentKeyIdx < apiKeys.size) {
            try {
                // 3. API 호출을 위한 Request DTO 생성
                val requestDto = EstateFetchRequestDto(
                    pageNo = page + 1,
                    numOfRows = pageSize,
                    serviceKey = apiKeys[currentKeyIdx] // 현재 인덱스의 API 키 주입
                )

                val responseDto = estateApiClient.fetchEstatePage(requestDto)

                // 4. 데이터 추출 및 결과 추가 (null 방어 로직 포함)
                responseDto.response?.body?.items?.let { items ->
                    currentResults.addAll(items.filterNotNull())
                }
                success = true

            } catch (e: HttpClientErrorException) {
                handleHttpClientError(e)
            } catch (e: HttpServerErrorException) {
                log.error("API 서버 내부 에러: {}", e.message)
                throw e
            } catch (e: Exception) {
                log.error("알 수 없는 에러: {}", e.message)
                throw e
            }
        }
    }

    private fun handleHttpClientError(e: HttpClientErrorException) {
        when (e.statusCode) {
            HttpStatus.TOO_MANY_REQUESTS -> { // 429
                log.warn("API 한도 초과! 키를 교체합니다.")
                switchApiKey()
            }
            HttpStatus.UNAUTHORIZED -> { // 401
                log.error("잘못된 API 키입니다. 다음 키로 넘어갑니다.")
                switchApiKey()
            }
            else -> {
                log.error("클라이언트 에러 발생: {}", e.message)
                throw e
            }
        }
    }

    private fun switchApiKey() {
        currentKeyIdx++
        if (currentKeyIdx >= apiKeys.size) {
            throw RuntimeException("모든 API 키가 소진되었습니다.")
        }
        log.info("새로운 API 키로 전환: Index {}", currentKeyIdx)
    }
}
