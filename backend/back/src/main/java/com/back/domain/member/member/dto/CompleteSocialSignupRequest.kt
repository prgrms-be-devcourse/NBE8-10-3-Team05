package com.back.domain.member.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CompleteSocialSignupRequest(

    @field:NotBlank(message = "rrnFront는 필수입니다.")
    @field:Size(min = 6, max = 6, message = "rrnFront는 6자리여야 합니다.")
    val rrnFront: String,

    @field:NotBlank(message = "rrnBackFirst는 필수입니다.")
    @field:Size(min = 1, max = 1, message = "rrnBackFirst는 1자리여야 합니다.")
    val rrnBackFirst: String
)
