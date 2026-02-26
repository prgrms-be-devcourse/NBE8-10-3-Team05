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
        fun builder(): PolicyBuilder = PolicyBuilder()

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

class PolicyBuilder {
    var plcyNo: String? = null
    var plcyNm: String? = null
    var plcyKywdNm: String? = null
    var plcyExplnCn: String? = null
    var plcySprtCn: String? = null
    var sprvsnInstCdNm: String? = null
    var operInstCdNm: String? = null
    var aplyPrdSeCd: String? = null
    var bizPrdBgngYmd: String? = null
    var bizPrdEndYmd: String? = null
    var plcyAplyMthdCn: String? = null
    var aplyUrlAddr: String? = null
    var sbmsnDcmntCn: String? = null
    var sprtTrgtMinAge: String? = null
    var sprtTrgtMaxAge: String? = null
    var sprtTrgtAgeLmtYn: String? = null
    var mrgSttsCd: String? = null
    var earnCndSeCd: String? = null
    var earnMinAmt: String? = null
    var earnMaxAmt: String? = null
    var zipCd: String? = null
    var jobCd: String? = null
    var schoolCd: String? = null
    var aplyYmd: String? = null
    var sBizCd: String? = null
    var rawJson: String? = null

    fun plcyNo(v: String?) = apply { plcyNo = v }
    fun plcyNm(v: String?) = apply { plcyNm = v }
    fun plcyKywdNm(v: String?) = apply { plcyKywdNm = v }
    fun plcyExplnCn(v: String?) = apply { plcyExplnCn = v }
    fun plcySprtCn(v: String?) = apply { plcySprtCn = v }
    fun sprvsnInstCdNm(v: String?) = apply { sprvsnInstCdNm = v }
    fun operInstCdNm(v: String?) = apply { operInstCdNm = v }
    fun aplyPrdSeCd(v: String?) = apply { aplyPrdSeCd = v }
    fun bizPrdBgngYmd(v: String?) = apply { bizPrdBgngYmd = v }
    fun bizPrdEndYmd(v: String?) = apply { bizPrdEndYmd = v }
    fun plcyAplyMthdCn(v: String?) = apply { plcyAplyMthdCn = v }
    fun aplyUrlAddr(v: String?) = apply { aplyUrlAddr = v }
    fun sbmsnDcmntCn(v: String?) = apply { sbmsnDcmntCn = v }
    fun sprtTrgtMinAge(v: String?) = apply { sprtTrgtMinAge = v }
    fun sprtTrgtMaxAge(v: String?) = apply { sprtTrgtMaxAge = v }
    fun sprtTrgtAgeLmtYn(v: String?) = apply { sprtTrgtAgeLmtYn = v }
    fun mrgSttsCd(v: String?) = apply { mrgSttsCd = v }
    fun earnCndSeCd(v: String?) = apply { earnCndSeCd = v }
    fun earnMinAmt(v: String?) = apply { earnMinAmt = v }
    fun earnMaxAmt(v: String?) = apply { earnMaxAmt = v }
    fun zipCd(v: String?) = apply { zipCd = v }
    fun jobCd(v: String?) = apply { jobCd = v }
    fun schoolCd(v: String?) = apply { schoolCd = v }
    fun aplyYmd(v: String?) = apply { aplyYmd = v }
    fun sBizCd(v: String?) = apply { sBizCd = v }
    fun rawJson(v: String?) = apply { rawJson = v }

    fun build(): Policy = Policy(
        plcyNo = plcyNo,
        plcyNm = plcyNm,
        plcyKywdNm = plcyKywdNm,
        plcyExplnCn = plcyExplnCn,
        plcySprtCn = plcySprtCn,
        sprvsnInstCdNm = sprvsnInstCdNm,
        operInstCdNm = operInstCdNm,
        aplyPrdSeCd = aplyPrdSeCd,
        bizPrdBgngYmd = bizPrdBgngYmd,
        bizPrdEndYmd = bizPrdEndYmd,
        plcyAplyMthdCn = plcyAplyMthdCn,
        aplyUrlAddr = aplyUrlAddr,
        sbmsnDcmntCn = sbmsnDcmntCn,
        sprtTrgtMinAge = sprtTrgtMinAge,
        sprtTrgtMaxAge = sprtTrgtMaxAge,
        sprtTrgtAgeLmtYn = sprtTrgtAgeLmtYn,
        mrgSttsCd = mrgSttsCd,
        earnCndSeCd = earnCndSeCd,
        earnMinAmt = earnMinAmt,
        earnMaxAmt = earnMaxAmt,
        zipCd = zipCd,
        jobCd = jobCd,
        schoolCd = schoolCd,
        aplyYmd = aplyYmd,
        sBizCd = sBizCd,
        rawJson = rawJson
    )
}
