package com.back.domain.welfare.policy.dto

data class PolicyFetchRequestDto(
    val apiKeyNm: String?,
    val pageType: String?, // 1:목록, 2:상세
    val pageSize: String?,
    val rtnType: String?, // xml인지 json인지
)
