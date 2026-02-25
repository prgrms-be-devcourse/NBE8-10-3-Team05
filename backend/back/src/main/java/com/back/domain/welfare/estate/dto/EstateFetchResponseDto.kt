package com.back.domain.welfare.estate.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstateFetchResponseDto(
    @JvmField val response: Response? = null
) {
    data class Response(
        val header: HeaderDto? = null,
        @JvmField val body: BodyDto? = null
    ) {
        data class HeaderDto(
            val resultCode: String? = null,
            val resultMsg: String? = null
        )

        data class BodyDto(
            @JvmField val numOfRows: String? = null,
            @JvmField val pageNo: String? = null,
            @JvmField val totalCount: String? = null,
            @JvmField val items: List<EstateDto?>? = null // MutableList일 필요가 없다면 List 권장
        )
    }

    // Java 호환을 위한 빌더 클래스
    class EstateFetchResponseDtoBuilder internal constructor() {
        private var response: Response? = null

        @JsonProperty("response")
        fun response(response: Response?) = apply { this.response = response }

        fun build() = EstateFetchResponseDto(response)

        override fun toString() = "EstateFetchResponseDto.EstateFetchResponseDtoBuilder(response=$response)"
    }

    companion object {
        @JvmStatic
        fun builder() = EstateFetchResponseDtoBuilder()
    }
}
