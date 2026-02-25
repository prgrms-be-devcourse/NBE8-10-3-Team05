package com.back.domain.member.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 회원가입 요청 바디(JSON) 구조
data class JoinRequest(

    @field:NotBlank(message = "이름은 필수 입력값입니다")
    val name: String,

    @field:NotBlank(message = "이메일은 필수 입력값입니다")
    @field:Email(message = "이메일 형식이 올바르지 않습니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    val password: String,

    @field:NotBlank(message = "rrnFront는 필수 입력값입니다")
    @field:Size(min = 6, max = 6, message = "rrnFront는 6자리여야 합니다")
    val rrnFront: String,

    @field:NotBlank(message = "rrnBackFirst는 필수 입력값입니다")
    @field:Size(min = 1, max = 1, message = "rrnBackFirst는 1자리여야 합니다")
    val rrnBackFirst: String
)
