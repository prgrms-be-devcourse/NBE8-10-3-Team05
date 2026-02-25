package com.back.domain.welfare.estate.entity;

import org.springframework.util.StringUtils;

import com.back.domain.welfare.estate.dto.EstateDto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estate")
@Getter
@NoArgsConstructor
public class Estate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 공고 기본 정보 ---
    @Column(name = "pblanc_id")
    private String pblancId; // 공고 ID (고유 식별자 역할)

    @Column(name = "pblanc_nm")
    private String pblancNm; // 공고명 (예: [시흥정왕 1블록 행복주택] 예비 입주자모집)

    @Column(name = "sttus_nm")
    private String sttusNm; // 공고 상태 (예: 모집중, 마감)

    @Column(name = "rcrit_pblanc_de")
    private String rcritPblancDe; // 모집 공고일 (YYYYMMDD)

    @Column(name = "begin_de")
    private String beginDe; // 모집 시작일 (접수 시작)

    @Column(name = "end_de")
    private String endDe; // 모집 종료일 (접수 마감)

    // --- 주택 및 공급 정보 ---
    @Column(name = "suply_ho_co")
    private String suplyHoCo; // 공급 호수 (몇 세대를 모집하는지)

    @Column(name = "house_sn")
    private Integer houseSn; // 주택 일련번호

    @Column(name = "suply_instt_nm")
    private String suplyInsttNm; // 공급 기관명 (예: LH, SH 등)

    @Column(name = "house_ty_nm")
    private String houseTyNm; // 주택 유형 (예: 아파트, 오피스텔)

    @Column(name = "suply_ty_nm")
    private String suplyTyNm; // 공급 유형 (예: 행복주택, 전세임대, 국민임대)

    // --- 위치 및 단지 정보 ---
    @Column(name = "hsmp_nm")
    private String hsmpNm; // 단지명

    @Column(name = "brtc_nm")
    private String brtcNm; // 광역 시/도 명 (예: 경기도, 서울특별시)

    @Column(name = "signgu_nm")
    private String signguNm; // 시/군/구 명 (예: 시흥시, 강남구)

    @Column(name = "signgu_code")
    private String signguCode; // 시/군/구 코드

    @Column(name = "full_adres")
    private String fullAdres; // 전체 주소 (예: 경기도 시흥시 정왕동 1799-2)

    // --- 금액 및 기타 ---
    @Column(name = "rent_gtn")
    private Long rentGtn; // 임대 보증금 (단위: 원)

    @Column(name = "mt_rntchrg")
    private Long mtRntchrg; // 월 임대료 (단위: 원)

    @Column(length = 1000) // URL은 길어질 수 있으므로 길이 넉넉하게 지정
    private String url; // 모집 공고 상세 페이지 URL

    public Estate(EstateDto dto) {
        this.pblancId = dto.pblancId();
        this.pblancNm = dto.pblancNm();
        this.sttusNm = dto.sttusNm();
        this.rcritPblancDe = dto.rcritPblancDe();
        this.beginDe = dto.beginDe();
        this.endDe = dto.endDe();

        this.suplyHoCo = dto.suplyHoCo();
        this.houseSn = dto.houseSn();
        this.suplyInsttNm = dto.suplyInsttNm();
        this.houseTyNm = dto.houseTyNm();
        this.suplyTyNm = dto.suplyTyNm();

        this.hsmpNm = dto.hsmpNm();
        this.brtcNm = dto.brtcNm();
        this.signguCode = extractSignguCode(dto.pnu());
        this.signguNm = dto.signguNm();
        this.fullAdres = dto.fullAdres();

        this.rentGtn = dto.rentGtn();
        this.mtRntchrg = dto.mtRntchrg();
        this.url = dto.url();
    }

    private String extractSignguCode(String pnu) {
        if (StringUtils.hasText(pnu) && pnu.length() >= 5) {
            return pnu.substring(0, 5);
        }
        return "";
    }
}
