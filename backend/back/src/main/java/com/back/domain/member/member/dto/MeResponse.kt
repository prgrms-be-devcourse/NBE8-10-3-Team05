package com.back.domain.member.member.dto

data class MeResponse(
    val id: Long,
    val name: String,
    val email: String? // Member.email이 nullable이라 안전하게 nullable로 유지
)
