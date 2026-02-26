package com.back.domain.member.geo.dto

data class AddressDto( // 카카오 우편번호 검색 API 제공
    val postcode: String?,  // 우편번호
    val addressName: String?,  // 전체 주소
    val sigunguCode: String?,  // 41135 시/군/구 코드
    val bCode: String?,  // 4113511000	법정동/법정리 코드
    @JvmField val roadAddress: String?,  // 도로명주소
    val sigungu: String?,  // 시/군/구 이름 "성남시 분당구"
    val sido: String?,  // 도/시 이름 "경기"
    // 카카오 Local API 제공
    // 도로명 주소로 가져온다.

    val hCode: String?,  // "4514069000" 행정동 코드
    val latitude: Double?,  // 위도
    val longitude: Double? // 경도
) {
    class AddressDtoBuilder internal constructor() {
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
        fun postcode(postcode: String): AddressDtoBuilder {
            this.postcode = postcode
            return this
        }

        fun addressName(addressName: String?): AddressDtoBuilder {
            this.addressName = addressName
            return this
        }

        fun sigunguCode(sigunguCode: String?): AddressDtoBuilder {
            this.sigunguCode = sigunguCode
            return this
        }

        fun bCode(bCode: String?): AddressDtoBuilder {
            this.bCode = bCode
            return this
        }

        fun roadAddress(roadAddress: String?): AddressDtoBuilder {
            this.roadAddress = roadAddress
            return this
        }

        fun sigungu(sigungu: String?): AddressDtoBuilder {
            this.sigungu = sigungu
            return this
        }

        fun sido(sido: String?): AddressDtoBuilder {
            this.sido = sido
            return this
        }

        fun hCode(hCode: String?): AddressDtoBuilder {
            this.hCode = hCode
            return this
        }

        fun latitude(latitude: Double?): AddressDtoBuilder {
            this.latitude = latitude
            return this
        }

        fun longitude(longitude: Double?): AddressDtoBuilder {
            this.longitude = longitude
            return this
        }

        fun build(): AddressDto {
            return AddressDto(
                this.postcode,
                this.addressName,
                this.sigunguCode,
                this.bCode,
                this.roadAddress,
                this.sigungu,
                this.sido,
                this.hCode,
                this.latitude,
                this.longitude
            )
        }

        override fun toString(): String {
            return "AddressDto.AddressDtoBuilder(postcode=" + this.postcode + ", addressName=" + this.addressName + ", sigunguCode=" + this.sigunguCode + ", bCode=" + this.bCode + ", roadAddress=" + this.roadAddress + ", sigungu=" + this.sigungu + ", sido=" + this.sido + ", hCode=" + this.hCode + ", latitude=" + this.latitude + ", longitude=" + this.longitude + ")"
        }
    }

    companion object {
        fun of(base: AddressDto, geo: GeoApiResponseDto.Address): AddressDto {
            return AddressDto(
                base.postcode,
                base.addressName,
                base.sigunguCode,
                base.bCode,
                base.roadAddress,
                base.sigungu,
                base.sido,
                geo.hCode,  // 새로운 값 주입
                geo.y?.toDouble(),  // 위도 (문자열 -> Double 변환)
                geo.x?.toDouble() // 경도 (문자열 -> Double 변환)
            )
        }

        @JvmStatic
        fun builder(): AddressDtoBuilder {
            return AddressDtoBuilder()
        }
    }
}
