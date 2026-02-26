package com.back.domain.welfare.estate.dto

data class EstateFetchRequestDto(
    val serviceKey: String? = null,        // 공공데이터포털에서 받은 인증키
    val brtcCode: String? = null,          // 광역시도 코드
    val signguCode: String? = null,         // 시군구 코드
    @JvmField val numOfRows: Int? = 10,     // 조회될 목록의 페이지당 데이터 개수 (기본값:10)
    @JvmField val pageNo: Int? = 1,         // 조회될 페이지의 번호 (기본값:1)
    val suplyTy: String? = null,           // 공급유형
    val houseTy: String? = null,           // 주택유형
    val lfstsTyAt: String? = null,         // 전세형 모집 여부 (Y/N 등)
    val bassMtRntchrgSe: String? = null,    // 월임대료 구분
    val yearMtBegin: String? = null,       // 모집공고월시작(YYYYMM)
    val yearMtEnd: String? = null          // 모집공고월시작(YYYYMM)
) {

}
