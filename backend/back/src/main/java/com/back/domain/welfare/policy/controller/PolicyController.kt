package com.back.domain.welfare.policy.controller

import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.dto.PolicyElasticSearchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.search.PolicySearchCondition
import com.back.domain.welfare.policy.service.PolicyElasticSearchService
import com.back.domain.welfare.policy.service.PolicyFetchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@RequestMapping("/api/v1/welfare/policy")
class PolicyController(  // ✅ 생성자 주입으로 변경
    private val policyFetchService: PolicyFetchService,
    private val policyElasticSearchService: PolicyElasticSearchService
) {

    @GetMapping("/search")
    @Throws(IOException::class)
    fun search(policyElasticSearchRequestDto: PolicyElasticSearchRequestDto): List<PolicyDocument?> {
        val condition = PolicySearchCondition(
            keyword = policyElasticSearchRequestDto.keyword,
            age = policyElasticSearchRequestDto.age,
            earn = policyElasticSearchRequestDto.earn,
            regionCode = policyElasticSearchRequestDto.regionCode,
            jobCode = policyElasticSearchRequestDto.jobCode,
            schoolCode = policyElasticSearchRequestDto.schoolCode,
            marriageStatus = policyElasticSearchRequestDto.marriageStatus,
            keywords = policyElasticSearchRequestDto.keywords
        )
        return policyElasticSearchService.search(
            condition, policyElasticSearchRequestDto.from, policyElasticSearchRequestDto.size
        )
    }

    @GetMapping("/list")
    @Throws(IOException::class)
    fun policy() {
        val requestDto = PolicyFetchRequestDto(null, "1", "100", "json")
        policyFetchService.fetchAndSavePolicies(requestDto)  // ✅ !! 불필요
    }
}
