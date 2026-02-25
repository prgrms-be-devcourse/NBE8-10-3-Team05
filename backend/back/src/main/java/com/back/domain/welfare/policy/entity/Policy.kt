package com.back.domain.welfare.policy.entity;

import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "policy")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plcy_no", nullable = false, unique = true)
    private String plcyNo; // 정책번호

    private String plcyNm; // 정책명
    private String plcyKywdNm; // 정책키워드명

    @Lob
    private String plcyExplnCn; // 정책설명내용
    // 너무 긴 반환값을 가지는 칼럼에 한해 큰 객체로 저장하는 어노테이션 추가
    @Lob
    private String plcySprtCn; // 정책지원내용

    private String sprvsnInstCdNm; // 주관기관코드명(주관기관명)
    private String operInstCdNm; // 운영기관코드명(운영기관명)

    private String aplyPrdSeCd; // 신청기간구분코드(상시, 특정기간 등)

    private String bizPrdBgngYmd; // 사업기간시작일자
    private String bizPrdEndYmd; // 사업기간종료일자

    @Lob
    private String plcyAplyMthdCn; // 정책신청방법내용

    @Lob
    private String aplyUrlAddr; // 신청URL주소

    @Lob
    private String sbmsnDcmntCn; // 제출서류내용

    private String sprtTrgtMinAge; // 지원대상최소연령
    private String sprtTrgtMaxAge; // 지원대상최대연령
    private String sprtTrgtAgeLmtYn; // 지원대상연령제한여부

    private String mrgSttsCd; // 결혼상태코드
    private String earnCndSeCd; // 소득조건구분코드(무관, 연소득, 기타)
    private String earnMinAmt; // 소득최소금액
    private String earnMaxAmt; // 소득최대금액

    @Lob
    private String zipCd; // 정책거주지역코드

    private String jobCd; // 정책취업요건코드
    private String schoolCd; // 정책학력요건코드

    private String aplyYmd; // 신청기간
    private String sBizCd; // 정책특화요건코드

    // 원본 JSON, 추가 요청 조건이 있을 경우 빠르게 반영 가능하도록 저장
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawJson;

    public static Policy from(PolicyFetchResponseDto.PolicyItem item, String rawJson) {
        return Policy.builder()
                .plcyNo(item.plcyNo())
                .plcyNm(item.plcyNm())
                .plcyKywdNm(item.plcyKywdNm())
                .plcyExplnCn(item.plcyExplnCn())
                .plcySprtCn(item.plcySprtCn())
                .sprvsnInstCdNm(item.sprvsnInstCdNm())
                .operInstCdNm(item.operInstCdNm())
                .aplyPrdSeCd(item.aplyPrdSeCd())
                .bizPrdBgngYmd(item.bizPrdBgngYmd())
                .bizPrdEndYmd(item.bizPrdEndYmd())
                .plcyAplyMthdCn(item.plcyAplyMthdCn())
                .aplyUrlAddr(item.aplyUrlAddr())
                .sbmsnDcmntCn(item.sbmsnDcmntCn())
                .sprtTrgtMinAge(item.sprtTrgtMinAge())
                .sprtTrgtMaxAge(item.sprtTrgtMaxAge())
                .sprtTrgtAgeLmtYn(item.sprtTrgtAgeLmtYn())
                .mrgSttsCd(item.mrgSttsCd())
                .earnCndSeCd(item.earnCndSeCd())
                .earnMinAmt(item.earnMinAmt())
                .earnMaxAmt(item.earnMaxAmt())
                .zipCd(item.zipCd())
                .jobCd(item.jobCd())
                .schoolCd(item.schoolCd())
                .aplyYmd(item.aplyYmd())
                .sBizCd(item.sbizCd()) // record의 sbizCd -> Entity의 sBizCd 매핑
                .rawJson(rawJson) // 원본 JSON 문자열 저장
                .build();
    }
}
