package com.back.domain.member.policyaply.service

import com.back.domain.member.member.entity.Member
import com.back.domain.member.policyaply.dto.DeleteApplicationResponseDto
import com.back.domain.member.policyaply.entity.Application
import com.back.domain.member.policyaply.repository.ApplicationRepository
import com.back.domain.welfare.policy.repository.PolicyRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class PolicyApplyService(
    private val applicationRepository: ApplicationRepository,
    private val policyRepository: PolicyRepository
) {

    // ✅ member.id reflection 헬퍼
    private fun getMemberId(member: Member): Long {
        return Member::class.java.getDeclaredField("id")
            .apply { isAccessible = true }
            .get(member) as Long
    }

    fun getApplicationList(member: Member): List<Application> {
        return applicationRepository.findAllByApplicant_Id(getMemberId(member))
            ?.filterNotNull()
            ?: emptyList()
    }

    fun addApplication(member: Member, policyId: Int): Application? {
        val policy = policyRepository.findPolicyById(policyId) ?: return null

        val application = Application()
        application.policy = policy
        application.applicant = member

        applicationRepository.save(application)
        return application
    }

    fun deleteApplication(member: Member, applicationId: Long): DeleteApplicationResponseDto {
        val application = applicationRepository.getApplicationById(applicationId)
            ?: return DeleteApplicationResponseDto(HttpStatus.NOT_FOUND.value(), "신청 내역을 찾지 못했습니다.")

        val applicantId = application.applicant?.let { getMemberId(it) }
        if (applicantId != getMemberId(member)) {
            return DeleteApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "삭제 권한이 없습니다.")
        }

        applicationRepository.delete(application)
        return DeleteApplicationResponseDto(HttpStatus.OK.value(), "성공적으로 삭제되었습니다.")
    }
}
