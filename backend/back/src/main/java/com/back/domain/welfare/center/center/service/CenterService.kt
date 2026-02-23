package com.back.domain.welfare.center.center.service

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto
import com.back.domain.welfare.center.center.dto.CenterApiRequestDto.Companion.from
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import com.back.domain.welfare.center.center.dto.dtoToEntity
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.center.repository.CenterRepository
import com.back.standard.util.SidoNormalizer
import lombok.SneakyThrows
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
class CenterService(
    private val centerApiService: CenterApiService,
    private val centerRepository: CenterRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun getCenterData(): MutableList<Center> {
        val pageSize = 100
        val firstResponse = centerApiService.fetchCenter(CenterApiRequestDto.from(1, pageSize))

        val totalCnt = firstResponse.totalCount
        val totalPages = ceil(totalCnt.toDouble() / pageSize).toInt()

        val allCenterList = firstResponse.data
            .map { it.dtoToEntity() }
            .toMutableList()

        centerRepository.saveAll(allCenterList)

        // 2페이지부터 순회
        for (pageNo in 2..totalPages) {
            log.debug("fetchCenter pageNo : {} ,pageSize : {} 실행", pageNo, pageSize)

            val nextResponse = centerApiService.fetchCenter(CenterApiRequestDto.from(pageNo, pageSize))

            val updatedCenterList = nextResponse.data
                .map { it.dtoToEntity() }
                .toMutableList()

            if (updatedCenterList.isNotEmpty()) {
                centerRepository.saveAll(updatedCenterList)
                allCenterList.addAll(updatedCenterList)
            }

            Thread.sleep(500)
        }

        return allCenterList
    }

    @Cacheable(value = ["center"], key = "#sido + ':' + #signguNm")
    fun searchCenterList(sido: String?, signguNm: String?): List<Center> {
        val normalizedSido = SidoNormalizer.normalizeSido(sido)
        return centerRepository.findByAddressContaining(normalizedSido) ?: emptyList()
    }
}
