package com.back.domain.welfare.policy.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.dto.PolicyElasticSearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.search.PolicySearchCondition;
import com.back.domain.welfare.policy.service.PolicyElasticSearchService;
import com.back.domain.welfare.policy.service.PolicyFetchService;
import com.back.domain.welfare.policy.service.PolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/policy")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;
    private final PolicyFetchService policyFetchService;
    private final PolicyElasticSearchService policyElasticSearchService;

    @GetMapping("/search")
    public List<PolicyDocument> search(PolicyElasticSearchRequestDto policyElasticSearchRequestDto) throws IOException {
        PolicySearchCondition condition = PolicySearchCondition.builder()
                .keyword(policyElasticSearchRequestDto.keyword())
                .age(policyElasticSearchRequestDto.age())
                .earn(policyElasticSearchRequestDto.earn())
                .regionCode(policyElasticSearchRequestDto.regionCode())
                .jobCode(policyElasticSearchRequestDto.jobCode())
                .schoolCode(policyElasticSearchRequestDto.schoolCode())
                .marriageStatus(policyElasticSearchRequestDto.marriageStatus())
                .keywords(policyElasticSearchRequestDto.keywords())
                .build();
        return policyElasticSearchService.search(
                condition, policyElasticSearchRequestDto.from(), policyElasticSearchRequestDto.size());
    }

    @GetMapping("/list")
    public void getPolicy() throws IOException {
        PolicyFetchRequestDto requestDto = new PolicyFetchRequestDto(null, "1", "100", "json");

        policyFetchService.fetchAndSavePolicies(requestDto);
    }
}
