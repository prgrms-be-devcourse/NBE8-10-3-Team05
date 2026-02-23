package com.back.domain.welfare.policy.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.entity.Policy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PolicyDocumentMapper {

    public PolicyDocument toDocument(Policy policy) {
        return PolicyDocument.builder()
                .policyId(policy.getId())
                .plcyNo(policy.getPlcyNo())
                .plcyNm(policy.getPlcyNm())

                // 나이
                .minAge(parseInteger(policy.getSprtTrgtMinAge()))
                .maxAge(parseInteger(policy.getSprtTrgtMaxAge()))
                .ageLimited(parseBoolean(policy.getSprtTrgtAgeLmtYn()))

                // 소득
                .earnCondition(policy.getEarnCndSeCd())
                .earnMin(parseInteger(policy.getEarnMinAmt()))
                .earnMax(parseInteger(policy.getEarnMaxAmt()))

                // 대상 조건
                .regionCode(policy.getZipCd())
                .jobCode(policy.getJobCd())
                .schoolCode(policy.getSchoolCd())
                .marriageStatus(policy.getMrgSttsCd())

                // 태그 / 분류
                .keywords(parseKeywords(policy.getPlcyKywdNm()))
                .specialBizCode(policy.getSBizCd())

                // 검색용 텍스트
                .description(buildDescription(policy.getPlcyExplnCn(), policy.getPlcySprtCn()))
                .build();
    }

    /* ===== 유틸 메서드 ===== */

    private Integer parseInteger(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null) return null;
        return "Y".equalsIgnoreCase(value);
    }

    private List<String> parseKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return Collections.emptyList();
        }
        // 예: "청년,주거,취업"
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String buildDescription(String... texts) {
        return Arrays.stream(texts)
                .filter(t -> t != null && !t.isBlank())
                .reduce((a, b) -> a + " " + b)
                .orElse(null);
    }
}
