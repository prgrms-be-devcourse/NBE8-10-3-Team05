package com.back.domain.welfare.policy.document

data class PolicyDocument(
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

    // 검색용 텍스트
    var description: String? = null
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var policyId: Int? = null
        private var plcyNo: String? = null
        private var plcyNm: String? = null
        private var minAge: Int? = null
        private var maxAge: Int? = null
        private var ageLimited: Boolean? = null
        private var earnCondition: String? = null
        private var earnMin: Int? = null
        private var earnMax: Int? = null
        private var regionCode: String? = null
        private var jobCode: String? = null
        private var schoolCode: String? = null
        private var marriageStatus: String? = null
        private var keywords: MutableList<String?>? = null
        private var specialBizCode: String? = null
        private var description: String? = null

        fun policyId(v: Int?) = apply { policyId = v }
        fun plcyNo(v: String?) = apply { plcyNo = v }
        fun plcyNm(v: String?) = apply { plcyNm = v }
        fun minAge(v: Int?) = apply { minAge = v }
        fun maxAge(v: Int?) = apply { maxAge = v }
        fun ageLimited(v: Boolean?) = apply { ageLimited = v }
        fun earnCondition(v: String?) = apply { earnCondition = v }
        fun earnMin(v: Int?) = apply { earnMin = v }
        fun earnMax(v: Int?) = apply { earnMax = v }
        fun regionCode(v: String?) = apply { regionCode = v }
        fun jobCode(v: String?) = apply { jobCode = v }
        fun schoolCode(v: String?) = apply { schoolCode = v }
        fun marriageStatus(v: String?) = apply { marriageStatus = v }
        fun keywords(v: MutableList<String?>?) = apply { keywords = v }
        fun specialBizCode(v: String?) = apply { specialBizCode = v }
        fun description(v: String?) = apply { description = v }

        fun build() = PolicyDocument(
            policyId, plcyNo, plcyNm,
            minAge, maxAge, ageLimited,
            earnCondition, earnMin, earnMax,
            regionCode, jobCode, schoolCode, marriageStatus,
            keywords, specialBizCode, description
        )
    }
}
