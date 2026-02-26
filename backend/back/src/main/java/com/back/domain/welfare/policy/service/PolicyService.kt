package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto
import com.back.domain.welfare.policy.repository.PolicyRepositoryCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PolicyService(
    private val policyRepository: PolicyRepositoryCustom
) {

    fun search(request: PolicySearchRequestDto): List<PolicySearchResponseDto> {
        return policyRepository.search(request)
    }
}