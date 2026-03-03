package com.back.domain.welfare.center.center.dto

import com.back.domain.welfare.center.center.entity.Center
import com.fasterxml.jackson.annotation.JsonProperty

data class CenterApiResponseDto(
    val page: Int,
    val perPage: Int,
    val totalCount: Int,
    val currentCount: Int,
    val matchCount: Int,
    val data: List<CenterDto>
) {
    data class CenterDto(
        @field: JsonProperty("연번")
        val id: Int?,

        @field: JsonProperty("시도")
        val city: String?,

        @field: JsonProperty("기관명")
        val facilityName: String?,

        @field: JsonProperty("주소")
        val address: String?,

        @field: JsonProperty("전화번호")
        val phoneNumber: String?,

        @field: JsonProperty("운영주체")
        val operator: String?,

        @field: JsonProperty("법인유형")
        val corporationType: String?
    )
}

fun CenterApiResponseDto.CenterDto.dtoToEntity(): Center = Center(
    location = city,
    name = facilityName,
    address = address,
    contact = phoneNumber,
    operator = operator,
    corpType = corporationType
)
