package com.back.domain.welfare.policy.search;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicySearchCondition {

    // 키워드
    private String keyword;

    // 나이
    private Integer age;

    // 소득
    private Integer earn;

    // 지역 / 직업 / 학력
    private String regionCode;
    private String jobCode;
    private String schoolCode;

    // 결혼 여부
    private String marriageStatus;

    // 태그
    private List<String> keywords;
}
