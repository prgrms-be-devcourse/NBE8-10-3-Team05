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
    fun getEstateLocation(
        @RequestParam keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int): EstateSearchResonseDto {

        val (k1, k2) = keyword?.trim()
            ?.split(Regex("\\s+"))
            ?.filter { it.isNotBlank() }
            ?.let { tokens ->
                if (tokens.isEmpty()) "" to ""
                else {
                    val first = SidoNormalizer.normalizeSido(tokens[0])
                    val second = if (tokens.size >= 2) SidoNormalizer.normalizeSido(tokens[1]) else first
                    first to second
                }
            } ?: ("" to "")

        // TODO: 추후 공고 중복 필터링 로직 추가 필요
        val estatePage = estateService.searchEstateLocation(k1,k2,page,size)

        return EstateSearchResonseDto(
            estatePage.content,
            estatePage.totalElements.toInt(),
            estatePage.totalPages,
            estatePage.number)
    }

    @GetMapping("/regions")
    fun getEstateRegions(): EstateRegionResponseDto {
        return EstateRegionResponseDto(regionCache.regionList)
    }
}
