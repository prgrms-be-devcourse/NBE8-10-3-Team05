package com.back.domain.member.member.entity

import com.back.global.enumtype.EducationLevel
import com.back.global.enumtype.EmploymentStatus
import com.back.global.enumtype.MarriageStatus
import com.back.global.enumtype.SpecialStatus
import jakarta.persistence.*
import com.back.domain.member.geo.dto.*

@Entity
@Table(name = "member_detail")
class MemberDetail private constructor(
// private constructor 때문에 외부에서 주 생성자 직접 호출 불가
// 무조건 create() 메서드를 통해서만 MemberDetail 생성 가능

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(length = 5)
    var regionCode: String? = null,

    @Enumerated(EnumType.STRING)
    var marriageStatus: MarriageStatus? = null,

    var income: Int? = null,

    @Enumerated(EnumType.STRING)
    var employmentStatus: EmploymentStatus? = null,

    @Enumerated(EnumType.STRING)
    var educationLevel: EducationLevel? = null,

    @Enumerated(EnumType.STRING)
    var specialStatus: SpecialStatus? = null,

    @Embedded
    var address: Address? = null
){
    @Id
    var id: Long? = null
        private set // setter 차단, getter로 조회만 가능하도록

    companion object {
        @JvmStatic
        fun create(member: Member): MemberDetail {
            return MemberDetail(member = member)
        }
    }

    fun update(
        regionCode: String?,
        marriageStatus: MarriageStatus?,
        income: Int?,
        employmentStatus: EmploymentStatus?,
        educationLevel: EducationLevel?,
        specialStatus: SpecialStatus?
    ) {
        this.regionCode = regionCode
        this.marriageStatus = marriageStatus
        this.income = income
        this.employmentStatus = employmentStatus
        this.educationLevel = educationLevel
        this.specialStatus = specialStatus
    }

    fun updateAddress(geoAddress: AddressDto) {
        this.address = Address.from(geoAddress)
    }

    @Embeddable
    class Address(
        var postcode: String? = null,
        var addressName: String? = null,
        var sigunguCode: String? = null,
        var bCode: String? = null,
        var roadAddress: String? = null,
        var sigungu: String? = null,
        var sido: String? = null,
        var hCode: String? = null,
        var latitude: Double? = null,
        var longitude: Double? = null
    ) {
        companion object {
            fun from(dto: AddressDto): Address {
                return Address(
                    postcode = dto.postcode,
                    addressName = dto.addressName,
                    sigunguCode = dto.sigunguCode,
                    bCode = dto.bCode,
                    roadAddress = dto.roadAddress,
                    sigungu = dto.sigungu,
                    sido = dto.sido,
                    hCode = dto.hCode,
                    latitude = dto.latitude,
                    longitude = dto.longitude
                )
            }
        }
    }
}

