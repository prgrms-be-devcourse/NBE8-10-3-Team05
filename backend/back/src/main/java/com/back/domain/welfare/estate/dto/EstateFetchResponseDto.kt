package com.back.domain.welfare.estate.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstateFetchResponseDto(
    val response: Response? = null
) {
    data class Response(
        val header: HeaderDto,
        val body: BodyDto
    ) {
        data class HeaderDto(
            val resultCode: String,
            val resultMsg: String
        )

        data class BodyDto(
            val numOfRows: String,
            val pageNo: String,
            val totalCount: String,
            val items: List<EstateDto>// MutableList일 필요가 없다면 List 권장
        )
    }
}
