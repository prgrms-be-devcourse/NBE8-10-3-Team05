package com.back.domain.welfare.policy.dto

/**
 * 청년정책 Open API 응답 DTO
 * - API 구조 그대로 표현
 */
data class PolicyFetchResponseDto(
    val resultCode: Int,
    val resultMessage: String?,
    @JvmField //추후 삭제필요
    val result: Result?
) {
    // Java record 스타일 호환 accessor
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
        val pagging: Pagging?,
        @JvmField //추후 삭제필요
        val youthPolicyList: List<PolicyItem>?
    ) {
        // Java record 스타일 호환 accessor
        fun youthPolicyList() = youthPolicyList

        ////마이그레이션 안정성을 위해 임시로 builder패턴 직접구현. 최종적으로는 삭제 필요
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
        val totCount: Int,
        val pageNum: Int,
        val pageSize: Int
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
        val plcyNo: String?,
        val bscPlanCycl: String?,
        val bscPlanPlcyWayNo: String?,
        val bscPlanFcsAsmtNo: String?,
        val bscPlanAsmtNo: String?,
        val pvsnInstGroupCd: String?,
        val plcyPvsnMthdCd: String?,
        val plcyAprvSttsCd: String?,
        val plcyNm: String?,
        val plcyKywdNm: String?,
        val plcyExplnCn: String?,
        val lclsfNm: String?,
        val mclsfNm: String?,
        val plcySprtCn: String?,
        val sprvsnInstCd: String?,
        val sprvsnInstCdNm: String?,
        val sprvsnInstPicNm: String?,
        val operInstCd: String?,
        val operInstCdNm: String?,
        val operInstPicNm: String?,
        val sprtSclLmtYn: String?,
        val aplyPrdSeCd: String?,
        val bizPrdSeCd: String?,
        val bizPrdBgngYmd: String?,
        val bizPrdEndYmd: String?,
        val bizPrdEtcCn: String?,
        val plcyAplyMthdCn: String?,
        val srngMthdCn: String?,
        val aplyUrlAddr: String?,
        val sbmsnDcmntCn: String?,
        val etcMttrCn: String?,
        val refUrlAddr1: String?,
        val refUrlAddr2: String?,
        val sprtSclCnt: String?,
        val sprtArvlSeqYn: String?,
        val sprtTrgtMinAge: String?,
        val sprtTrgtMaxAge: String?,
        val sprtTrgtAgeLmtYn: String?,
        val mrgSttsCd: String?,
        val earnCndSeCd: String?,
        val earnMinAmt: String?,
        val earnMaxAmt: String?,
        val earnEtcCn: String?,
        val addAplyQlfcCndCn: String?,
        val ptcpPrpTrgtCn: String?,
        val inqCnt: String?,
        val rgtrInstCd: String?,
        val rgtrInstCdNm: String?,
        val rgtrUpInstCd: String?,
        val rgtrUpInstCdNm: String?,
        val rgtrHghrkInstCd: String?,
        val rgtrHghrkInstCdNm: String?,
        val zipCd: String?,
        val plcyMajorCd: String?,
        val jobCd: String?,
        val schoolCd: String?,
        val aplyYmd: String?,
        val frstRegDt: String?,
        val lastMdfcnDt: String?,
        val sbizCd: String?
    )


}