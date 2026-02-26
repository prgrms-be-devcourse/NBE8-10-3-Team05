package com.back.domain.welfare.policy.document

class PolicyDocument(
    var policyId: Int? = null,
    var plcyNo: String? = null,
    var plcyNm: String? = null,

    // 나이
    var minAge: Int? = null,
    var maxAge: Int? = null,
    var ageLimited: Boolean? = null,

    // 소득
    var earnCondition: String? = null,
    var earnMin: Int? = null,
    var earnMax: Int? = null,

    // 대상 조건
    var regionCode: String? = null,
    var jobCode: String? = null,
    var schoolCode: String? = null,
    var marriageStatus: String? = null,

    // 태그 / 분류
    var keywords: MutableList<String?>? = null,
    var specialBizCode: String? = null,

    // 검색용 텍스트 (선택)
    var description: String? = null
)
