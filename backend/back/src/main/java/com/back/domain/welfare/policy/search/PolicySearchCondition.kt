package com.back.domain.welfare.policy.search

data class PolicySearchCondition(
    val keyword: String? = null,
    val age: Int? = null,
    val earn: Int? = null,
    val regionCode: String? = null,
    val jobCode: String? = null,
    val schoolCode: String? = null,
    val marriageStatus: String? = null,
    val keywords: List<String?>? = null
)
