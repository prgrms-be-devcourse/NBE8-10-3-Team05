package com.back.domain.welfare.policy.entity

import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import jakarta.persistence.*

@Entity
@Table(name = "policy")
class Policy(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "plcy_no", nullable = false, unique = true)
    var plcyNo: String? = null,

    var plcyNm: String? = null,
    var plcyKywdNm: String? = null,

    @Lob
    var plcyExplnCn: String? = null,

    @Lob
    var plcySprtCn: String? = null,

    var sprvsnInstCdNm: String? = null,
    var operInstCdNm: String? = null,
    var aplyPrdSeCd: String? = null,
    var bizPrdBgngYmd: String? = null,
    var bizPrdEndYmd: String? = null,

    @Lob
    var plcyAplyMthdCn: String? = null,

    @Lob
    var aplyUrlAddr: String? = null,

    @Lob
    var sbmsnDcmntCn: String? = null,

    var sprtTrgtMinAge: String? = null,
    var sprtTrgtMaxAge: String? = null,
    var sprtTrgtAgeLmtYn: String? = null,
    var mrgSttsCd: String? = null,
    var earnCndSeCd: String? = null,
    var earnMinAmt: String? = null,
    var earnMaxAmt: String? = null,

    @Lob
    var zipCd: String? = null,

    var jobCd: String? = null,
    var schoolCd: String? = null,
    var aplyYmd: String? = null,
    var sBizCd: String? = null,

    @Lob
    @Column(columnDefinition = "TEXT")
    var rawJson: String? = null
) {
    companion object {
        @JvmStatic
        fun from(item: PolicyItem, rawJson: String?): Policy {
            return Policy(
                plcyNo = item.plcyNo,
                plcyNm = item.plcyNm,
                plcyKywdNm = item.plcyKywdNm,
                plcyExplnCn = item.plcyExplnCn,
                plcySprtCn = item.plcySprtCn,
                sprvsnInstCdNm = item.sprvsnInstCdNm,
                operInstCdNm = item.operInstCdNm,
                aplyPrdSeCd = item.aplyPrdSeCd,
                bizPrdBgngYmd = item.bizPrdBgngYmd,
                bizPrdEndYmd = item.bizPrdEndYmd,
                plcyAplyMthdCn = item.plcyAplyMthdCn,
                aplyUrlAddr = item.aplyUrlAddr,
                sbmsnDcmntCn = item.sbmsnDcmntCn,
                sprtTrgtMinAge = item.sprtTrgtMinAge,
                sprtTrgtMaxAge = item.sprtTrgtMaxAge,
                sprtTrgtAgeLmtYn = item.sprtTrgtAgeLmtYn,
                mrgSttsCd = item.mrgSttsCd,
                earnCndSeCd = item.earnCndSeCd,
                earnMinAmt = item.earnMinAmt,
                earnMaxAmt = item.earnMaxAmt,
                zipCd = item.zipCd,
                jobCd = item.jobCd,
                schoolCd = item.schoolCd,
                aplyYmd = item.aplyYmd,
                sBizCd = item.sbizCd,
                rawJson = rawJson
            )
        }
    }
}
