package com.back.domain.welfare.center.lawyer.controller

import com.back.domain.welfare.center.lawyer.dto.LawyerReq
import com.back.domain.welfare.center.lawyer.dto.LawyerRes
import com.back.domain.welfare.center.lawyer.service.LawyerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/welfare/center/location/lawyer")
class LawyerController(
    private val lawyerService: LawyerService
) {

    @GetMapping
    fun searchLawyersByDistrict(
        @Valid lawyerReq: LawyerReq,
        @PageableDefault(size = 30, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<Page<LawyerRes>> {
        val lawyers: Page<LawyerRes> = lawyerService.searchByDistrict(lawyerReq, pageable)

        return ResponseEntity.ok(lawyers)
    }
}
