package com.back.global.springBatch.policy

import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.entity.Policy.Companion.from
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class PolicyApiItemProcessor : ItemProcessor<PolicyItem, Policy> {
    override fun process(policyItem: PolicyItem): Policy? {
        return from(policyItem, "")
    }
}
