package com.back.domain.welfare.policy.dto

import com.querydsl.core.annotations.QueryProjection

data class PolicySearchResponseDto @QueryProjection constructor (
    val id: Int?,
    val plcyNo: String?,
    val plcyNm: String?,
    val plcyExplnCn: String?,
    val plcySprtCn: String?,
    val plcyKywdNm: String?,
    val sprtTrgtMinAge: String?,
    val sprtTrgtMaxAge: String?,
    val zipCd: String?,
    val schoolCd: String?,
    val jobCd: String?,
    val earnMinAmt: String?,
    val earnMaxAmt: String?,
    val aplyYmd: String?,
    val aplyUrlAddr: String?,
    val aplyMthdCn: String?,
    val sbmsnDcmntCn: String?,
    val operInstCdNm: String?
)
