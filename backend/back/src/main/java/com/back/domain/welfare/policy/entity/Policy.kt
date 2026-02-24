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
    var plcyNo: String? = null, // 정책번호

    var plcyNm: String? = null, // 정책명
    var plcyKywdNm: String? = null, // 정책키워드명

    @Lob
    var plcyExplnCn: String? = null, // 정책설명내용

    // 너무 긴 반환값을 가지는 칼럼에 한해 큰 객체로 저장하는 어노테이션 추가
    @Lob
    var plcySprtCn: String? = null, // 정책지원내용

    var sprvsnInstCdNm: String? = null, // 주관기관코드명(주관기관명)
    var operInstCdNm: String? = null, // 운영기관코드명(운영기관명)

    var aplyPrdSeCd: String? = null, // 신청기간구분코드(상시, 특정기간 등)

    var bizPrdBgngYmd: String? = null, // 사업기간시작일자
    var bizPrdEndYmd: String? = null, // 사업기간종료일자

    @Lob
    var plcyAplyMthdCn: String? = null, // 정책신청방법내용

    @Lob
    var aplyUrlAddr: String? = null, // 신청URL주소

    @Lob
    var sbmsnDcmntCn: String? = null, // 제출서류내용

    var sprtTrgtMinAge: String? = null, // 지원대상최소연령
    var sprtTrgtMaxAge: String? = null, // 지원대상최대연령
    var sprtTrgtAgeLmtYn: String? = null, // 지원대상연령제한여부

    var mrgSttsCd: String? = null, // 결혼상태코드
    var earnCndSeCd: String? = null, // 소득조건구분코드(무관, 연소득, 기타)
    var earnMinAmt: String? = null, // 소득최소금액
    var earnMaxAmt: String? = null, // 소득최대금액

    @Lob
    var zipCd: String? = null, // 정책거주지역코드

    var jobCd: String? = null, // 정책취업요건코드
    var schoolCd: String? = null, // 정책학력요건코드

    var aplyYmd: String? = null, // 신청기간
    var sBizCd: String? = null, // 정책특화요건코드

    // 원본 JSON, 추가 요청 조건이 있을 경우 빠르게 반영 가능하도록 저장
    @Lob
    @Column(columnDefinition = "TEXT")
    var rawJson: String? = null,
) {
    companion object { //마이그레이션 안정성을 위해 임시로 builder패턴 직접구현. 최종적으로는 삭제 필요
        @JvmStatic
        fun builder() = Builder()

        @JvmStatic
        fun from(item: PolicyItem, rawJson: String?): Policy = Policy(
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
            sBizCd = item.sbizCd, // record의 sbizCd -> Entity의 sBizCd 매핑
            rawJson = rawJson, // 원본 JSON 문자열 저장
        )
    }

    class Builder {
        private var id: Int? = null
        private var plcyNo: String? = null
        private var plcyNm: String? = null
        private var plcyKywdNm: String? = null
        private var plcyExplnCn: String? = null
        private var plcySprtCn: String? = null
        private var sprvsnInstCdNm: String? = null
        private var operInstCdNm: String? = null
        private var aplyPrdSeCd: String? = null
        private var bizPrdBgngYmd: String? = null
        private var bizPrdEndYmd: String? = null
        private var plcyAplyMthdCn: String? = null
        private var aplyUrlAddr: String? = null
        private var sbmsnDcmntCn: String? = null
        private var sprtTrgtMinAge: String? = null
        private var sprtTrgtMaxAge: String? = null
        private var sprtTrgtAgeLmtYn: String? = null
        private var mrgSttsCd: String? = null
        private var earnCndSeCd: String? = null
        private var earnMinAmt: String? = null
        private var earnMaxAmt: String? = null
        private var zipCd: String? = null
        private var jobCd: String? = null
        private var schoolCd: String? = null
        private var aplyYmd: String? = null
        private var sBizCd: String? = null
        private var rawJson: String? = null

        fun id(id: Int?) = apply { this.id = id }
        fun plcyNo(plcyNo: String?) = apply { this.plcyNo = plcyNo }
        fun plcyNm(plcyNm: String?) = apply { this.plcyNm = plcyNm }
        fun plcyKywdNm(plcyKywdNm: String?) = apply { this.plcyKywdNm = plcyKywdNm }
        fun plcyExplnCn(plcyExplnCn: String?) = apply { this.plcyExplnCn = plcyExplnCn }
        fun plcySprtCn(plcySprtCn: String?) = apply { this.plcySprtCn = plcySprtCn }
        fun sprvsnInstCdNm(sprvsnInstCdNm: String?) = apply { this.sprvsnInstCdNm = sprvsnInstCdNm }
        fun operInstCdNm(operInstCdNm: String?) = apply { this.operInstCdNm = operInstCdNm }
        fun aplyPrdSeCd(aplyPrdSeCd: String?) = apply { this.aplyPrdSeCd = aplyPrdSeCd }
        fun bizPrdBgngYmd(bizPrdBgngYmd: String?) = apply { this.bizPrdBgngYmd = bizPrdBgngYmd }
        fun bizPrdEndYmd(bizPrdEndYmd: String?) = apply { this.bizPrdEndYmd = bizPrdEndYmd }
        fun plcyAplyMthdCn(plcyAplyMthdCn: String?) = apply { this.plcyAplyMthdCn = plcyAplyMthdCn }
        fun aplyUrlAddr(aplyUrlAddr: String?) = apply { this.aplyUrlAddr = aplyUrlAddr }
        fun sbmsnDcmntCn(sbmsnDcmntCn: String?) = apply { this.sbmsnDcmntCn = sbmsnDcmntCn }
        fun sprtTrgtMinAge(sprtTrgtMinAge: String?) = apply { this.sprtTrgtMinAge = sprtTrgtMinAge }
        fun sprtTrgtMaxAge(sprtTrgtMaxAge: String?) = apply { this.sprtTrgtMaxAge = sprtTrgtMaxAge }
        fun sprtTrgtAgeLmtYn(sprtTrgtAgeLmtYn: String?) = apply { this.sprtTrgtAgeLmtYn = sprtTrgtAgeLmtYn }
        fun mrgSttsCd(mrgSttsCd: String?) = apply { this.mrgSttsCd = mrgSttsCd }
        fun earnCndSeCd(earnCndSeCd: String?) = apply { this.earnCndSeCd = earnCndSeCd }
        fun earnMinAmt(earnMinAmt: String?) = apply { this.earnMinAmt = earnMinAmt }
        fun earnMaxAmt(earnMaxAmt: String?) = apply { this.earnMaxAmt = earnMaxAmt }
        fun zipCd(zipCd: String?) = apply { this.zipCd = zipCd }
        fun jobCd(jobCd: String?) = apply { this.jobCd = jobCd }
        fun schoolCd(schoolCd: String?) = apply { this.schoolCd = schoolCd }
        fun aplyYmd(aplyYmd: String?) = apply { this.aplyYmd = aplyYmd }
        fun sBizCd(sBizCd: String?) = apply { this.sBizCd = sBizCd }
        fun rawJson(rawJson: String?) = apply { this.rawJson = rawJson }

        fun build() = Policy(
            id = id,
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
            rawJson = rawJson,
        )
    }
}
