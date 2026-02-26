package com.back.domain.welfare.estate.entity

import com.back.domain.welfare.estate.dto.EstateDto
import jakarta.persistence.*
import org.springframework.util.StringUtils

@Entity
@Table(name = "estate")
// JPA가 지연 로딩을 위해 Proxy를 만들 수 있도록 'all-open' 플러그인을 쓰거나 직접 open을 붙입니다.
class Estate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // --- 공고 기본 정보 ---
    @Column(name = "pblanc_id")
    var pblancId: String? = null,

    @Column(name = "pblanc_nm")
    var pblancNm: String? = null,

    @Column(name = "sttus_nm")
    var sttusNm: String? = null,

    @Column(name = "rcrit_pblanc_de")
    var rcritPblancDe: String? = null,

    @Column(name = "begin_de")
    var beginDe: String? = null,

    @Column(name = "end_de")
    var endDe: String? = null,

    // --- 주택 및 공급 정보 ---
    @Column(name = "suply_ho_co")
    var suplyHoCo: String? = null,

    @Column(name = "house_sn")
    var houseSn: Int? = null,

    @Column(name = "suply_instt_nm")
    var suplyInsttNm: String? = null,

    @Column(name = "house_ty_nm")
    var houseTyNm: String? = null,

    @Column(name = "suply_ty_nm")
    var suplyTyNm: String? = null,

    // --- 위치 및 단지 정보 ---
    @Column(name = "hsmp_nm")
    var hsmpNm: String? = null,

    @Column(name = "brtc_nm")
    var brtcNm: String? = null,

    @Column(name = "signgu_nm")
    var signguNm: String? = null,

    @Column(name = "signgu_code")
    var signguCode: String? = null,

    @Column(name = "full_adres")
    var fullAdres: String? = null,

    // --- 금액 및 기타 ---
    @Column(name = "rent_gtn")
    var rentGtn: Long? = null,

    @Column(name = "mt_rntchrg")
    var mtRntchrg: Long? = null,

    @Column(length = 1000)
    var url: String? = null
) {
    constructor(dto: EstateDto) : this(
        pblancId = dto.pblancId,
        pblancNm = dto.pblancNm,
        sttusNm = dto.sttusNm,
        rcritPblancDe = dto.rcritPblancDe,
        beginDe = dto.beginDe,
        endDe = dto.endDe,
        suplyHoCo = dto.suplyHoCo,
        houseSn = dto.houseSn,
        suplyInsttNm = dto.suplyInsttNm,
        houseTyNm = dto.houseTyNm,
        suplyTyNm = dto.suplyTyNm,
        hsmpNm = dto.hsmpNm,
        brtcNm = dto.brtcNm,
        signguNm = dto.signguNm,
        signguCode = dto.pnu?.takeIf { it.length >= 5 }?.substring(0, 5) ?: "",
        fullAdres = dto.fullAdres,
        rentGtn = dto.rentGtn,
        mtRntchrg = dto.mtRntchrg,
        url = dto.url
    )
}
