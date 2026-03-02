package com.back.domain.welfare.center.center.controller

import com.back.domain.welfare.center.center.dto.CenterSearchResponseDto
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.center.service.CenterService
import com.back.domain.welfare.estate.dto.EstateSearchResonseDto
import com.back.standard.util.SidoNormalizer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/welfare/center")
class CenterController(private val centerService: CenterService) {
    @GetMapping("/location")
    fun getCenterList(
        @RequestParam keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int): CenterSearchResponseDto {
        // 키워드 정규화 및 분리 (EstateController의 로직과 통일)
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

        // 서비스에서 Page 객체 받아오기
        val centerPage = centerService.searchCenterList(k1, k2, page, size)

        return CenterSearchResponseDto(
            centerList = centerPage.content,
            totalCount = centerPage.totalElements.toInt(),
            totalPages = centerPage.totalPages,
            currentPage = centerPage.number
        )
    }
}
