package com.back.domain.welfare.policy.dto;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

public record PolicyElasticSearchRequestDto(
        String zipCd,
        String schoolCode,
        String jobCode,
        String keyword,
        Integer age,
        Integer earn,
        String regionCode,
        String marriageStatus,
        List<String> keywords,
        int from,
        int size) {
    @QueryProjection
    public PolicyElasticSearchRequestDto {}
    ;
}
