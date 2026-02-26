package com.back.domain.welfare.estate.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstateDto(
    @JsonProperty("suplyHoCo") val suplyHoCo: String? = null,
    @JsonProperty("pblancId") val pblancId: String? = null,
    @JsonProperty("houseSn") val houseSn: Int? = null,
    @JsonProperty("sttusNm") val sttusNm: String? = null,
    @JsonProperty("pblancNm") val pblancNm: String? = null, // [시흥정왕 1블록 행복주택] 예비 입주자모집
    @JsonProperty("suplyInsttNm") val suplyInsttNm: String? = null,
    @JsonProperty("houseTyNm") val houseTyNm: String? = null, // "아파트"
    @JsonProperty("suplyTyNm") val suplyTyNm: String? = null, // "행복주택", "전세임대"
    @JsonProperty("rcritPblancDe") val rcritPblancDe: String? = null,
    @JsonProperty("url") val url: String? = null,
    @JsonProperty("hsmpNm") val hsmpNm: String? = null,
    @JsonProperty("brtcNm") val brtcNm: String? = null, // "경기도",
    @JsonProperty("signguNm") val signguNm: String? = null, // "시흥시"
    @JsonProperty("fullAdres") val fullAdres: String? = null, // "경기도 시흥시 정왕동 1799-2 ",
    @JsonProperty("rentGtn") val rentGtn: Long? = null,
    @JsonProperty("mtRntchrg") val mtRntchrg: Long? = null,
    @JsonProperty("beginDe") val beginDe: String? = null, // "20260116"
    @JsonProperty("endDe") val endDe: String? = null, // "20260116"
    @JsonProperty("pnu") val pnu: String? = null // 시군구 코드 추출을 위해 추가
) {

}
