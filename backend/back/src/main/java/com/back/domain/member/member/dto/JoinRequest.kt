package com.back.domain.member.member.dto

// 회원가입 요청 바디(JSON) 구조
data class JoinRequest(
    val name: String?,
    val email: String?,
    val password: String?,
    val rrnFront: String?,
    val rrnBackFirst: String?
)
