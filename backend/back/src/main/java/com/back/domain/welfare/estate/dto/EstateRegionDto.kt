package com.back.domain.welfare.estate.dto

data class EstateRegionDto(
    val name: String? = null,
    val parentName: String? = null, // 부모 이름 (null이면 최상위)
    val level: Int = 0             // level도 인자로 받을 수 있게 생성자에 포함
)
