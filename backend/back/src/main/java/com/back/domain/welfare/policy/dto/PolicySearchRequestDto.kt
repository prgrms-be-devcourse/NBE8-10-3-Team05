package com.back.domain.welfare.policy.dto

data class PolicySearchRequestDto(
    val sprtTrgtMinAge: Int?,
    val sprtTrgtMaxAge: Int?,
    val zipCd: String?,
    val schoolCd: String?,
    val jobCd: String?,
    val earnMinAmt: Int?,
    val earnMaxAmt: Int?
)
