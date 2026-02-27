package com.back.domain.welfare.center.center.dto

import com.back.domain.welfare.center.center.entity.Center

data class CenterApiResponseDto(
    val page: Int,
    val perPage: Int,
    val totalCount: Int,
    val currentCount: Int,
    val matchCount: Int,
    val data: List<CenterDto>
) {
    data class CenterDto(
        val id: Int?,

        val city: String?,

        val facilityName: String?,

        val address: String?,

        val phoneNumber: String?,

        val operator: String?,

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
