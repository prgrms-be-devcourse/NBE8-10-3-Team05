package com.back.domain.welfare.estate.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstateDto(
    @field:JsonProperty("suplyHoCo") val suplyHoCo: String? = null,
    @field:JsonProperty("pblancId") val pblancId: String? = null,
    @field:JsonProperty("houseSn") val houseSn: Int? = null,
    @field:JsonProperty("sttusNm") val sttusNm: String? = null,
    @field:JsonProperty("pblancNm") val pblancNm: String? = null, // [시흥정왕 1블록 행복주택] 예비 입주자모집
    @field:JsonProperty("suplyInsttNm") val suplyInsttNm: String? = null,
    @field:JsonProperty("houseTyNm") val houseTyNm: String? = null, // "아파트"
    @field:JsonProperty("suplyTyNm") val suplyTyNm: String? = null, // "행복주택", "전세임대"
    @field:JsonProperty("rcritPblancDe") val rcritPblancDe: String? = null,
    @field:JsonProperty("url") val url: String? = null,
    @field:JsonProperty("hsmpNm") val hsmpNm: String? = null,
    @field:JsonProperty("brtcNm") val brtcNm: String? = null, // "경기도",
    @field:JsonProperty("signguNm") val signguNm: String? = null, // "시흥시"
    @field:JsonProperty("fullAdres") val fullAdres: String? = null, // "경기도 시흥시 정왕동 1799-2 ",
    @field:JsonProperty("rentGtn") val rentGtn: Long? = null,
    @field:JsonProperty("mtRntchrg") val mtRntchrg: Long? = null,
    @field:JsonProperty("beginDe") val beginDe: String? = null, // "20260116"
    @field:JsonProperty("endDe") val endDe: String? = null, // "20260116"
    @field:JsonProperty("pnu") val pnu: String? = null // 시군구 코드 추출을 위해 추가
) {

}
