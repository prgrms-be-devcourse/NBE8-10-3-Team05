package com.back.domain.welfare.estate.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstateFetchResponseDto(
    @field:JsonProperty("response")
    val response: Response? = null
) {
    data class Response(
        @field:JsonProperty("header")
        val header: HeaderDto,
        @field:JsonProperty("body")
        val body: BodyDto
    ) {
        data class HeaderDto(
            @field:JsonProperty("resultCode")
            val resultCode: String,
            @field:JsonProperty("resultMsg")
            val resultMsg: String
        )

        data class BodyDto(
            @field:JsonProperty("numOfRows")
            val numOfRows: String,
            @field:JsonProperty("pageNo")
            val pageNo: String,
            @field:JsonProperty("totalCount")
            val totalCount: String,
            @field:JsonProperty("item")
            val items: List<EstateDto>
        )
    }
}
