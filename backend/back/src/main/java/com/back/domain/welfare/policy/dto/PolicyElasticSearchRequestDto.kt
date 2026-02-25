package com.back.domain.welfare.policy.dto

import com.querydsl.core.annotations.QueryProjection


class PolicyElasticSearchRequestDto @QueryProjection constructor(
    val zipCd: String?,
    @JvmField val schoolCode: String?,
    @JvmField val jobCode: String?,
    @JvmField val keyword: String?,
    @JvmField val age: Int?,
    @JvmField val earn: Int?,
    @JvmField val regionCode: String?,
    @JvmField val marriageStatus: String?,
    @JvmField val keywords: MutableList<String?>?,
    @JvmField val from: Int,
    @JvmField val size: Int
)
