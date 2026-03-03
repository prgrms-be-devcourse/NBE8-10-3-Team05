package com.back.domain.welfare.estate.dto

import com.back.domain.welfare.estate.entity.Estate

data class EstateSearchResonseDto(
    val estateList: List<Estate>,
    val totalCount: Int,
    val totalPages: Int,
    val currentPage: Int
)
