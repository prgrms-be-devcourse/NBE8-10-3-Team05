package com.back.domain.member.geo.entity

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.dto.GeoApiResponseDto

data class Address( // 카카오 우편번호 검색 API 제공
    val postcode: String? = null,  // 우편번호
    val addressName: String? = null,  // 전체 주소
    val sigunguCode: String? = null,  // 41135 시/군/구 코드
    val bCode: String? = null,  // 4113511000  법정동/법정리 코드
    val roadAddress: String? = null,  // 도로명주소
    val sigungu: String? = null,  // 시/군/구 이름 "성남시 분당구"
    val sido: String? = null,  // 도/시 이름 "경기"
    // 카카오 Local API 제공
    // 도로명 주소로 가져온다.

    @JvmField val hCode: String? = null,  // "4514069000" 행정동 코드
    @JvmField val latitude: Double? = null,  // 위도
    @JvmField val longitude: Double? = null // 경도
) {
    class AddressBuilder internal constructor() {
        private var postcode: String? = null
        private var addressName: String? = null
        private var sigunguCode: String? = null
        private var bCode: String? = null
        private var roadAddress: String? = null
        private var sigungu: String? = null
        private var sido: String? = null
        private var hCode: String? = null
        private var latitude: Double? = null
        private var longitude: Double? = null

        fun postcode(postcode: String) = apply { this.postcode = postcode }
        fun addressName(addressName: String?) = apply { this.addressName = addressName }
        fun sigunguCode(sigunguCode: String?) = apply { this.sigunguCode = sigunguCode }
        fun bCode(bCode: String?) = apply { this.bCode = bCode }
        fun roadAddress(roadAddress: String?) = apply { this.roadAddress = roadAddress }
        fun sigungu(sigungu: String?) = apply { this.sigungu = sigungu }
        fun sido(sido: String?) = apply { this.sido = sido }
        fun hCode(hCode: String?) = apply { this.hCode = hCode }
        fun latitude(latitude: Double?) = apply { this.latitude = latitude }
        fun longitude(longitude: Double?) = apply { this.longitude = longitude }

        fun build(): Address = Address(
            postcode, addressName, sigunguCode, bCode, roadAddress,
            sigungu, sido, hCode, latitude, longitude
        )

        override fun toString(): String {
            return "Address.AddressBuilder(postcode=$postcode, addressName=$addressName, sigunguCode=$sigunguCode, bCode=$bCode, roadAddress=$roadAddress, sigungu=$sigungu, sido=$sido, hCode=$hCode, latitude=$latitude, longitude=$longitude)"
        }
    }

    companion object {
        @JvmStatic
        fun of(base: AddressDto, geo: GeoApiResponseDto.Address): Address {
            return Address(
                postcode = base.postcode,
                addressName = base.addressName,
                sigunguCode = base.sigunguCode,
                bCode = base.bCode,
                roadAddress = base.roadAddress,
                sigungu = base.sigungu,
                sido = base.sido,
                hCode = geo.hCode,  // 새로운 값 주입
                latitude = geo.y?.toDoubleOrNull(),  // 위도 (안전한 변환)
                longitude = geo.x?.toDoubleOrNull() // 경도 (안전한 변환)
            )
        }

        @JvmStatic
        fun builder(): AddressBuilder = AddressBuilder()
    }
}
