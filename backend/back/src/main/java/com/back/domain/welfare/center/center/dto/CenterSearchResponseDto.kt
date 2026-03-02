package com.back.domain.welfare.center.center.dto

import com.back.domain.welfare.center.center.entity.Center

data class CenterSearchResponseDto(
    val centerList: List<Center>,
    val totalCount: Int,
    val totalPages: Int,
    val currentPage: Int
)
