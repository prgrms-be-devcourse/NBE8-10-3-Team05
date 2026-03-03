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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

    @Cacheable(
        value = ["centerLocationSearch"],
        key = "#k1 + ':' + #k2 + ':p' + #page + ':s' + #size",
        unless = "#result.content.isEmpty()"
    )
    fun searchCenterList(k1: String, k2: String, page: Int, size: Int): Page<Center> {
        // 정규화 로직 (필요 시 서비스 계층에서 수행)
        val normalizedK1 = SidoNormalizer.normalizeSido(k1)
        val normalizedK2 = SidoNormalizer.normalizeSido(k2)

        // 최신순(id 내림차순) 정렬 및 페이지네이션 설정
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())

        // Repository 호출 (Page<Center> 반환)
        return centerRepository.findByKeywords(normalizedK1, normalizedK2, pageable)
    }
}
