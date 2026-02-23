package com.back.domain.welfare.center.lawyer.dto

import com.back.domain.welfare.center.lawyer.entity.Lawyer

data class LawyerRes(
    val id: String,
    val name: String,
    val corporation: String?,
    val districtArea1: String,
    val districtArea2: String?
) {
    constructor(lawyer: Lawyer) : this(
        lawyer.id,
        lawyer.name,
        lawyer.corporation,
        lawyer.districtArea1,
        lawyer.districtArea2
    )
}
