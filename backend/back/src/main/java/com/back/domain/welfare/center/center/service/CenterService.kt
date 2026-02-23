package com.back.domain.welfare.center.center.service

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto
import com.back.domain.welfare.center.center.dto.CenterApiRequestDto.Companion.from
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
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

    /**
     * 데이터를 가져와 저장하고 리스트를 반환합니다.
     * 코틀린에서는 Checked Exception을 강제하지 않으므로 SneakyThrows가 필요 없습니다.
     */
    @Transactional
    fun getCenterData(): MutableList<Center> {
        val pageSize = 100
        val firstResponse = centerApiService.fetchCenter(CenterApiRequestDto.from(1, pageSize))

        val totalCnt = firstResponse.totalCount
        val totalPages = ceil(totalCnt.toDouble() / pageSize).toInt()

        // 1페이지 데이터 변환 및 저장
        val allCenterList = firstResponse.data
            ?.mapNotNull { it?.let { Center.dtoToEntity(it) } }
            ?.toMutableList() ?: mutableListOf()

        centerRepository.saveAll(allCenterList)

        // 2페이지부터 순회
        for (pageNo in 2..totalPages) {
            log.debug("fetchCenter pageNo : {} ,pageSize : {} 실행", pageNo, pageSize)

            val nextResponse = centerApiService.fetchCenter(CenterApiRequestDto.from(pageNo, pageSize))

            // 데이터 변환 (Stream 대신 mapNotNull 사용)
            val updatedCenterList = nextResponse.data
                ?.mapNotNull { it?.let { Center.dtoToEntity(it) } }
                ?: emptyList()

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
