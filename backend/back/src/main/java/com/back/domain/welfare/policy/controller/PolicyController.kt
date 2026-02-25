package com.back.domain.welfare.policy.controller

import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.dto.PolicyElasticSearchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.search.PolicySearchCondition
import com.back.domain.welfare.policy.service.PolicyElasticSearchService
import com.back.domain.welfare.policy.service.PolicyFetchService
import com.back.domain.welfare.policy.service.PolicyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@RequestMapping("/api/v1/welfare/policy")
class PolicyController(                                         // ✅ 생성자 주입
    private val policyService: PolicyService,
    private val policyFetchService: PolicyFetchService,
    private val policyElasticSearchService: PolicyElasticSearchService
) {

    @GetMapping("/search")
    @Throws(IOException::class)
    fun search(dto: PolicyElasticSearchRequestDto): List<PolicyDocument?> {
        val condition = PolicySearchCondition(                  // ✅ builder() → 생성자 or 직접 할당
            keyword = dto.keyword,
            age = dto.age,
            earn = dto.earn,
            regionCode = dto.regionCode,
            jobCode = dto.jobCode,
            schoolCode = dto.schoolCode,
            marriageStatus = dto.marriageStatus,
            keywords = dto.keywords
        )
        return policyElasticSearchService.search(condition, dto.from, dto.size)
    }

    @GetMapping("/list")
    @Throws(IOException::class)
    fun getPolicy() {
        val requestDto = PolicyFetchRequestDto(null, "1", "100", "json")
        policyFetchService.fetchAndSavePolicies(requestDto)
    }
}
