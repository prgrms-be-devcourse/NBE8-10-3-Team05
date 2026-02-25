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
    // Java에서 .builder()를 호출하는 기존 코드를 유지하기 위한 컴패니언 객체
    companion object {
        @JvmStatic
        fun builder(): EstateFetchRequestDtoBuilder = EstateFetchRequestDtoBuilder()
    }

    // Kotlin 내부 로직으로 빌더를 구현하여 코드 양을 대폭 줄였습니다.
    class EstateFetchRequestDtoBuilder internal constructor() {
        private var serviceKey: String? = null
        private var brtcCode: String? = null
        private var signguCode: String? = null
        private var numOfRows: Int? = 10
        private var pageNo: Int? = 1
        private var suplyTy: String? = null
        private var houseTy: String? = null
        private var lfstsTyAt: String? = null
        private var bassMtRntchrgSe: String? = null
        private var yearMtBegin: String? = null
        private var yearMtEnd: String? = null

        fun serviceKey(serviceKey: String) = apply { this.serviceKey = serviceKey }
        fun brtcCode(brtcCode: String?) = apply { this.brtcCode = brtcCode }
        fun signguCode(signguCode: String?) = apply { this.signguCode = signguCode }
        fun numOfRows(numOfRows: Int?) = apply { this.numOfRows = numOfRows }
        fun pageNo(pageNo: Int?) = apply { this.pageNo = pageNo }
        fun suplyTy(suplyTy: String?) = apply { this.suplyTy = suplyTy }
        fun houseTy(houseTy: String?) = apply { this.houseTy = houseTy }
        fun lfstsTyAt(lfstsTyAt: String?) = apply { this.lfstsTyAt = lfstsTyAt }
        fun bassMtRntchrgSe(bassMtRntchrgSe: String?) = apply { this.bassMtRntchrgSe = bassMtRntchrgSe }
        fun yearMtBegin(yearMtBegin: String?) = apply { this.yearMtBegin = yearMtBegin }
        fun yearMtEnd(yearMtEnd: String?) = apply { this.yearMtEnd = yearMtEnd }

        fun build(): EstateFetchRequestDto = EstateFetchRequestDto(
            serviceKey, brtcCode, signguCode, numOfRows, pageNo,
            suplyTy, houseTy, lfstsTyAt, bassMtRntchrgSe, yearMtBegin, yearMtEnd
        )

        override fun toString(): String {
            return "EstateFetchRequestDto.EstateFetchRequestDtoBuilder(serviceKey=$serviceKey, brtcCode=$brtcCode, signguCode=$signguCode, numOfRows=$numOfRows, pageNo=$pageNo, suplyTy=$suplyTy, houseTy=$houseTy, lfstsTyAt=$lfstsTyAt, bassMtRntchrgSe=$bassMtRntchrgSe, yearMtBegin=$yearMtBegin, yearMtEnd=$yearMtEnd)"
        }
    }
}
