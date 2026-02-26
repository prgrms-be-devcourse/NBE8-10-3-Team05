package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.Member.LoginType
import com.back.global.enumtype.EducationLevel
import com.back.global.enumtype.EmploymentStatus
import com.back.global.enumtype.MarriageStatus
import com.back.global.enumtype.SpecialStatus

data class MemberDetailReq(
    // Member 정보
    val name: String? = null,
    val email: String? = null,
    val rrnFront: String? = null,
    val rrnBackFirst: String? = null,
    val type: LoginType? = null,
    val role: Member.Role? = null,

    // MemberDetail 정보
    val regionCode: String? = null,
    val marriageStatus: MarriageStatus? = null,
    val income: Int? = null,
    val employmentStatus: EmploymentStatus? = null,
    val educationLevel: EducationLevel? = null,
    val specialStatus: SpecialStatus? = null
)
