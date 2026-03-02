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
    fun getCenterList(@RequestParam keyword: String?): CenterSearchResponseDto {
        // null 또는 공백 문자열 체크를 한 번에 처리
        if (keyword.isNullOrBlank()) {
            return CenterSearchResponseDto(centerService.searchCenterList("", ""))
        }

        // 공백으로 분리 후 유효한 키워드만 필터링
        val keywords = keyword.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        // 키워드 정규화 로직 (1개일 때는 동일하게, 2개 이상일 때는 각각 처리)
        val keyword1 = SidoNormalizer.normalizeSido(keywords[0])
        val keyword2 = if (keywords.size >= 2) SidoNormalizer.normalizeSido(keywords[1]) else keyword1

        // TODO: 추후 공고 중복 필터링 로직 추가 필요
        val centerList = centerService.searchCenterList(keyword1, keyword2)

        return CenterSearchResponseDto(centerList)
    }
}
