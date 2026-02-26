package com.back.domain.member.member.service

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.entity.Address
import com.back.domain.member.geo.service.GeoService
import com.back.domain.member.member.dto.MemberDetailReq
import com.back.domain.member.member.dto.MemberDetailRes
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.MemberDetail
import com.back.domain.member.member.repository.MemberDetailRepository
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberDetailService(
    private val memberDetailRepository: MemberDetailRepository,
    private val memberRepository: MemberRepository,
    private val geoService: GeoService
) {


    @Transactional(readOnly = true)
    fun getDetail(memberId: Long): MemberDetailRes {
        val memberDetail = findByMemberId(memberId)
        return MemberDetailRes(memberDetail)
    }

    @Transactional
    fun findByMemberId(memberId: Long): MemberDetail {
        return memberDetailRepository.findByIdOrNull(memberId) ?: run {
            // 상세 정보가 없다?, 진짜 유저 자체가 없는지 확인.
            val member = memberRepository.findByIdOrNull(memberId)
                ?: throw IllegalArgumentException("존재하지 않는 회원입니다.")

            createDetail(member) // 유저는 있는데 상세 정보가 없다? detail 생성
        }
    }

    @Transactional
    fun createDetail(member: Member): MemberDetail {
        // 빌더 대신 MemberDetail.create로 새 memeberDetail 생성
        val detail = MemberDetail.create(member)
        return memberDetailRepository.save(detail)
    }

    @Transactional
    fun modify(memberId: Long, reqBody: MemberDetailReq) {
        val memberDetail = findByMemberId(memberId)
        val member = memberDetail.member

        updateMemberInfo(member, reqBody)

        memberDetail.update(
            regionCode = reqBody.regionCode,
            marriageStatus = reqBody.marriageStatus,
            income = reqBody.income,
            employmentStatus = reqBody.employmentStatus,
            educationLevel = reqBody.educationLevel,
            specialStatus = reqBody.specialStatus
        )
    }

    @Transactional
    fun updateAddress(memberId: Long, address: Address) {
        // 불완전한 address 정보로 geoService 이용 -> 완전한 address 정보 받아와 해당 memeberDetail의 address 업데이트해주는 기능

        val memberDetail = findByMemberId(memberId)

        val addressDto = AddressDto(
            address.postcode,
            address.addressName,
            address.sigunguCode,
            address.bCode,
            address.roadAddress,
            address.sigungu,
            address.sido,
            address.hCode,
            address.latitude,
            address.longitude
        )

        val enrichedAddress = geoService.getGeoCode(addressDto)
        // 입력된 불완전한 addressDto -> geoService 이용, 엄밀한 주소 데이터Address로 보충해줌

        val enrichedAddressDto = AddressDto(
            enrichedAddress.postcode,
            enrichedAddress.addressName,
            enrichedAddress.sigunguCode,
            enrichedAddress.bCode,
            enrichedAddress.roadAddress,
            enrichedAddress.sigungu,
            enrichedAddress.sido,
            enrichedAddress.hCode,
            enrichedAddress.latitude,
            enrichedAddress.longitude
        )

        memberDetail.updateAddress(enrichedAddressDto)
    }

    fun updateMemberInfo(member: Member, req: MemberDetailReq) {
        // reqBody의 멤버 정보값이 null이다? -> 기존 멤버 엔티티의 값 유지
        val newName = req.name ?: member.name
        val newEmail = req.email ?: member.email
        val newrrnFront = req.rrnFront ?: member.rrnFront
        val newrrnBackFirst = req.rrnBackFirst ?: member.rrnBackFirst

        // 이메일이 변경되는 경우에만 중복 체크
        if (member.email != newEmail) {
            // ACTIVE 회원 중에서만 중복 체크
            if (memberRepository.existsByEmailAndStatus(newEmail, Member.MemberStatus.ACTIVE)) {
                throw ServiceException("MEMBER_409", "이미 사용 중인 이메일입니다")
            }
        }
        member.updateInfo(newName, newEmail, newrrnFront, newrrnBackFirst)
    }
}


