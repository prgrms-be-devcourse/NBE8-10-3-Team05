package com.back.domain.welfare.policy.document;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDocument {

    private Integer policyId;
    private String plcyNo;
    private String plcyNm;

    // 나이
    private Integer minAge;
    private Integer maxAge;
    private Boolean ageLimited;

    // 소득
    private String earnCondition;
    private Integer earnMin;
    private Integer earnMax;

    // 대상 조건
    private String regionCode;
    private String jobCode;
    private String schoolCode;
    private String marriageStatus;

    // 태그 / 분류
    private List<String> keywords;
    private String specialBizCode;

    // 검색용 텍스트 (선택)
    private String description;
}
