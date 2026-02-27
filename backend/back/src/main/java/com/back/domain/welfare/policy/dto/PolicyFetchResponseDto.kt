package com.back.domain.welfare.policy.dto

/**
 * 청년정책 Open API 응답 DTO
 * - 모든 필드에 기본값을 추가하여 Jackson의 기본 생성자 부재 문제 해결
 */
data class PolicyFetchResponseDto(
    val resultCode: Int = 0,
    val resultMessage: String? = null,
    @JvmField
    val result: Result? = null
) {
    fun result() = result

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var resultCode: Int = 0
        private var resultMessage: String? = null
        private var result: Result? = null

        fun resultCode(value: Int) = apply { resultCode = value }
        fun resultMessage(value: String?) = apply { resultMessage = value }
        fun result(value: Result?) = apply { result = value }

        fun build() = PolicyFetchResponseDto(resultCode, resultMessage, result)
    }

    /**
     * result 영역
     */
    data class Result(
        val pagging: Pagging? = null,
        @JvmField
        val youthPolicyList: List<PolicyItem>? = null
    ) {
        fun youthPolicyList() = youthPolicyList

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        class Builder {
            private var pagging: Pagging? = null
            private var youthPolicyList: List<PolicyItem>? = null

            fun pagging(value: Pagging?) = apply { pagging = value }
            fun youthPolicyList(value: List<PolicyItem>?) = apply { youthPolicyList = value }

            fun build() = Result(pagging, youthPolicyList)
        }
    }

    /**
     * 페이징 정보
     */
    data class Pagging(
        val totCount: Int = 0,
        val pageNum: Int = 0,
        val pageSize: Int = 0
    ) {
        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        class Builder {
            private var totCount: Int = 0
            private var pageNum: Int = 0
            private var pageSize: Int = 0

            fun totCount(value: Int) = apply { totCount = value }
            fun pageNum(value: Int) = apply { pageNum = value }
            fun pageSize(value: Int) = apply { pageSize = value }

            fun build() = Pagging(totCount, pageNum, pageSize)
        }
    }

    /**
     * 청년 정책 단건
     */
    data class PolicyItem(
        val plcyNo: String? = null,
        val bscPlanCycl: String? = null,
        val bscPlanPlcyWayNo: String? = null,
        val bscPlanFcsAsmtNo: String? = null,
        val bscPlanAsmtNo: String? = null,
        val pvsnInstGroupCd: String? = null,
        val plcyPvsnMthdCd: String? = null,
        val plcyAprvSttsCd: String? = null,
        val plcyNm: String? = null,
        val plcyKywdNm: String? = null,
        val plcyExplnCn: String? = null,
        val lclsfNm: String? = null,
        val mclsfNm: String? = null,
        val plcySprtCn: String? = null,
        val sprvsnInstCd: String? = null,
        val sprvsnInstCdNm: String? = null,
        val sprvsnInstPicNm: String? = null,
        val operInstCd: String? = null,
        val operInstCdNm: String? = null,
        val operInstPicNm: String? = null,
        val sprtSclLmtYn: String? = null,
        val aplyPrdSeCd: String? = null,
        val bizPrdSeCd: String? = null,
        val bizPrdBgngYmd: String? = null,
        val bizPrdEndYmd: String? = null,
        val bizPrdEtcCn: String? = null,
        val plcyAplyMthdCn: String? = null,
        val srngMthdCn: String? = null,
        val aplyUrlAddr: String? = null,
        val sbmsnDcmntCn: String? = null,
        val etcMttrCn: String? = null,
        val refUrlAddr1: String? = null,
        val refUrlAddr2: String? = null,
        val sprtSclCnt: String? = null,
        val sprtArvlSeqYn: String? = null,
        val sprtTrgtMinAge: String? = null,
        val sprtTrgtMaxAge: String? = null,
        val sprtTrgtAgeLmtYn: String? = null,
        val mrgSttsCd: String? = null,
        val earnCndSeCd: String? = null,
        val earnMinAmt: String? = null,
        val earnMaxAmt: String? = null,
        val earnEtcCn: String? = null,
        val addAplyQlfcCndCn: String? = null,
        val ptcpPrpTrgtCn: String? = null,
        val inqCnt: String? = null,
        val rgtrInstCd: String? = null,
        val rgtrInstCdNm: String? = null,
        val rgtrUpInstCd: String? = null,
        val rgtrUpInstCdNm: String? = null,
        val rgtrHghrkInstCd: String? = null,
        val rgtrHghrkInstCdNm: String? = null,
        val zipCd: String? = null,
        val plcyMajorCd: String? = null,
        val jobCd: String? = null,
        val schoolCd: String? = null,
        val aplyYmd: String? = null,
        val frstRegDt: String? = null,
        val lastMdfcnDt: String? = null,
        val sbizCd: String? = null
    )
}
