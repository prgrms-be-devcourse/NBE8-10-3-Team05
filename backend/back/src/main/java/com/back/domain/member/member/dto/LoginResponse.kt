package com.back.domain.member.member.dto

// TODO: accessToken 빼는 것이 맞을 듯합니다?
data class LoginResponse(
    val memberId: Long,
    val name: String,
    val accessToken: String
)
