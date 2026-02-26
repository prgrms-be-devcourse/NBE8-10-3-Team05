package com.back.global.springBatch.policy

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.service.PolicyApiClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.util.concurrent.CopyOnWriteArrayList

@Component
class PolicyApiItemReader(
    private val policyApiClient: PolicyApiClient
) : AbstractPagingItemReader<PolicyItem>() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val totalCount = 2000
    private val apiKeys = listOf("1", "2", "3")
    private var currentKeyIdx = 0

    init {
        // 한번에 읽을 데이터 양 설정 (Kotlin Property 사용)
        pageSize = 1000
    }

    override fun doReadPage() {
        // 1. 결과 저장소 초기화 및 안전한 참조 생성
        val currentResults = results ?: CopyOnWriteArrayList<PolicyItem>().also { results = it }
        currentResults.clear()

        // 2. 읽어야 할 데이터가 totalCount를 넘으면 종료
        if (page * pageSize >= totalCount) return

        var success = false
        while (!success && currentKeyIdx < apiKeys.size) {
            try {
                // 3. API 호출을 위한 Request DTO 생성 (필요한 경우 serviceKey 주입 로직 추가)
                val requestDto = PolicyFetchRequestDto("", "", "", "")

                // API 클라이언트 호출
                val responseDto = policyApiClient.fetchPolicyPage(
                    requestDto,
                    page + 1,
                    pageSize
                )

                // 4. 데이터 추출 및 결과 추가 (Safe Call & Null Filter)
                responseDto.result()?.youthPolicyList()?.let { data ->
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
