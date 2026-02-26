package com.back.global.springBatch.center

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.service.CenterApiService
import org.slf4j.LoggerFactory
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.util.concurrent.CopyOnWriteArrayList

@Component
class CenterApiItemReader(
    private val centerApiService: CenterApiService
) : AbstractPagingItemReader<CenterDto>() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val totalCount = 481
    private val apiKeys = listOf("1", "2", "3")
    private var currentKeyIdx = 0

    init {
        pageSize = 1000
    }

    override fun doReadPage() {
        // 1. results 초기화 및 지역 변수 스냅샷 생성 (Thread-safety)
        val currentResults = results ?: CopyOnWriteArrayList<CenterDto>().also { results = it }
        currentResults.clear()

        // 2. 종료 조건 체크
        if (page * pageSize >= totalCount) return

        var success = false
        while (!success && currentKeyIdx < apiKeys.size) {
            try {
                val requestDto = CenterApiRequestDto.from(page + 1, pageSize)
                val responseDto = centerApiService.fetchCenter(requestDto)

                responseDto.data?.let { data ->
                    currentResults.addAll(data.filterNotNull())
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
            HttpStatus.TOO_MANY_REQUESTS -> {
                log.warn("API 한도 초과! 키를 교체합니다.")
                switchApiKey()
            }
            HttpStatus.UNAUTHORIZED -> {
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
