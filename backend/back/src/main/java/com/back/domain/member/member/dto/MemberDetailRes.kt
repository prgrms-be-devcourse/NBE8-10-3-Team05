package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.Member.LoginType
import com.back.domain.member.member.entity.MemberDetail
import com.back.global.enumtype.EducationLevel
import com.back.global.enumtype.EmploymentStatus
import com.back.global.enumtype.MarriageStatus
import com.back.global.enumtype.SpecialStatus
import java.time.LocalDateTime

data class MemberDetailRes(
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val name: String?,
    val email: String?,
    val rrnFront: String?,
    val rrnBackFirst: String?,
    val type: LoginType?,
    val role: Member.Role?,
    val regionCode: String?,
    val marriageStatus: MarriageStatus?,
    val income: Int?,
    val employmentStatus: EmploymentStatus?,
    val educationLevel: EducationLevel?,
    val specialStatus: SpecialStatus?,
    val postcode: String?,
    val roadAddress: String?,
    val hCode: String?,
    val latitude: Double?,
    val longitude: Double?
) {
    constructor(memberDetail: MemberDetail) : this(
        createdAt = memberDetail.member.createdAt,
        modifiedAt = memberDetail.member.modifiedAt,
        name = memberDetail.member.name,
        email = memberDetail.member.email,
        rrnFront = memberDetail.member.rrnFront,
        rrnBackFirst = memberDetail.member.rrnBackFirst,
        type = memberDetail.member.type,
        role = memberDetail.member.role,
        regionCode = memberDetail.regionCode,
        marriageStatus = memberDetail.marriageStatus,
        income = memberDetail.income,
        employmentStatus = memberDetail.employmentStatus,
        educationLevel = memberDetail.educationLevel,
        specialStatus = memberDetail.specialStatus,
        postcode = memberDetail.address?.postcode,
        roadAddress = memberDetail.address?.roadAddress,
        hCode = memberDetail.address?.hCode,
        latitude = memberDetail.address?.latitude,
        longitude = memberDetail.address?.longitude
    )
}
