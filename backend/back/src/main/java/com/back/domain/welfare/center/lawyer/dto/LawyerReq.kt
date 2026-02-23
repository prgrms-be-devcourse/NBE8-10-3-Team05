package com.back.domain.welfare.center.lawyer.dto

import jakarta.validation.constraints.NotBlank

data class LawyerReq(
    @field:NotBlank
    val area1: String,  // 시/도
    val area2: String? = null // 군/구
)
