package com.back.global.springBatch.policy

import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.entity.Policy.Companion.from
import com.back.domain.welfare.policy.repository.PolicyRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class PolicyApiItemProcessor( private val policyRepository: PolicyRepository) : ItemProcessor<PolicyItem, Policy> {
    private val existingIdMap: Map<String, Int> by lazy {
        policyRepository.findAll()
            .filter { it.plcyNo != null && it.id != null } // null 체크 by lazy를 위해서
            .associateBy({ it.plcyNo!! }, { it.id!! })
    }
    override fun process(policyItem: PolicyItem): Policy? {
        val policy = Policy.from(policyItem,"")

        // 2. 이제 타입이 딱 맞아서 에러가 사라집니다.
        val existingId = existingIdMap[policyItem.plcyNo]

        if (existingId != null) {
            policy.id = existingId
        }

        return policy
    }
}
