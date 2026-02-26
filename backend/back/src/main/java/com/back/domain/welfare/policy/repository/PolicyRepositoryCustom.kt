package com.back.domain.welfare.policy.repository

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto

interface PolicyRepositoryCustom {
    fun search(condition: PolicySearchRequestDto): List<PolicySearchResponseDto>
}
