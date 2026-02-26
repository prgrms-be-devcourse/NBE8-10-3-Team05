package com.back.domain.welfare.estate.controller

import com.back.domain.welfare.estate.dto.EstateRegionResponseDto
import com.back.domain.welfare.estate.dto.EstateSearchResonseDto
import com.back.domain.welfare.estate.entity.EstateRegionCache
import com.back.domain.welfare.estate.service.EstateService
import com.back.standard.util.SidoNormalizer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/welfare/estate")
class EstateController(
    private val estateService: EstateService,
    private val regionCache: EstateRegionCache
) {

    @GetMapping("/location")
    fun getEstateLocation(@RequestParam keyword: String?): EstateSearchResonseDto {
        // null 또는 공백 문자열 체크를 한 번에 처리
        if (keyword.isNullOrBlank()) {
            return EstateSearchResonseDto(estateService.searchEstateLocation("", ""))
        }

        // 공백으로 분리 후 유효한 키워드만 필터링
        val keywords = keyword.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        // 키워드 정규화 로직 (1개일 때는 동일하게, 2개 이상일 때는 각각 처리)
        val keyword1 = SidoNormalizer.normalizeSido(keywords[0])
        val keyword2 = if (keywords.size >= 2) SidoNormalizer.normalizeSido(keywords[1]) else keyword1

        // TODO: 추후 공고 중복 필터링 로직 추가 필요
        val estateList = estateService.searchEstateLocation(keyword1, keyword2)

        return EstateSearchResonseDto(estateList)
    }

    // Java의 Getter 메서드 스타일을 Kotlin의 프로퍼티 스타일로 간결하게 표현
    @GetMapping("/regions")
    fun getEstateRegions(): EstateRegionResponseDto {
        return EstateRegionResponseDto(regionCache.regionList)
    }
}
