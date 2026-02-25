package com.back.domain.welfare.estate.service

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto
import com.back.domain.welfare.estate.entity.Estate
import com.back.domain.welfare.estate.repository.EstateRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
class EstateService(
    private val estateRepository: EstateRepository,
    private val estateApiClient: EstateApiClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun fetchAndSaveAllEstates(): List<Estate> {
        val pageSize = 100
        val firstRequest = EstateFetchRequestDto(numOfRows = pageSize, pageNo = 1)

        // 첫 페이지 호출 및 전체 페이지 수 계산
        val firstResponse = estateApiClient.fetchEstatePage(firstRequest)
        val body = firstResponse.response?.body ?: return emptyList()

        val totalCnt = body.totalCount?.toInt() ?: 0
        val totalPages = ceil(totalCnt.toDouble() / pageSize).toInt()

        val allEstates = mutableListOf<Estate>()
        allEstates.addAll(estateListFromResponse(firstResponse))

        // 2페이지부터 순회
        for (pageNo in 2..totalPages) {
            val nextRequest = EstateFetchRequestDto(numOfRows = pageSize, pageNo = pageNo)
            val nextResponse = estateApiClient.fetchEstatePage(nextRequest)
            allEstates.addAll(estateListFromResponse(nextResponse))

            // API 서버 부하 방지
            Thread.sleep(500)
        }

        log.info("총 ${allEstates.size}건의 데이터를 저장합니다.")
        return estateRepository.saveAll(allEstates)
    }

    @Transactional
    fun saveEstateList(responseDto: EstateFetchResponseDto): List<Estate> {
        return estateRepository.saveAll(estateListFromResponse(responseDto))
    }

    private fun estateListFromResponse(responseDto: EstateFetchResponseDto): List<Estate> {
        val items = responseDto.response?.body?.items ?: return emptyList()

        // Java Stream 대신 Kotlin의 mapNotNull 활용
        return items.mapNotNull { dto ->
            dto?.let { Estate(it) }
        }
    }

    @Cacheable(value = ["estate"], key = "#keyword1 + ':' + #keyword2")
    fun searchEstateLocation(keyword1: String?, keyword2: String?): List<Estate> {
        return estateRepository.searchByKeywords(keyword1, keyword2) ?: emptyList()
    }
}
