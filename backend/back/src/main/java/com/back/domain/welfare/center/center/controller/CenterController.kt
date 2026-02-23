package com.back.domain.welfare.center.center.controller

import com.back.domain.welfare.center.center.dto.CenterSearchResponseDto
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.center.service.CenterService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/welfare/center")
class CenterController(private val centerService: CenterService) {
    @GetMapping("/location")
    fun getCenterList(@RequestParam sido: String?, @RequestParam signguNm: String?): CenterSearchResponseDto {
        val centerList= centerService.searchCenterList(sido, signguNm)

        return CenterSearchResponseDto(centerList)
    }
}
