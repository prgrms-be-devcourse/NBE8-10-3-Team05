package com.back.domain.welfare.center.center.dto

import com.back.domain.welfare.center.center.entity.Center

@JvmRecord
data class CenterSearchResponseDto(val centerList: List<Center>)
