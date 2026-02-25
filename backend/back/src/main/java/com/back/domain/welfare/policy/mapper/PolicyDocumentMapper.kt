package com.back.domain.welfare.policy.mapper

import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.entity.Policy
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component
import java.util.*

@Slf4j
@Component
class PolicyDocumentMapper {
    fun toDocument(policy: Policy): PolicyDocument {
        return PolicyDocument(
            policyId = policy.id,
            plcyNo = policy.plcyNo,
            plcyNm = policy.plcyNm,

            // 나이
            minAge = parseInteger(policy.sprtTrgtMinAge),
            maxAge = parseInteger(policy.sprtTrgtMaxAge),
            ageLimited = parseBoolean(policy.sprtTrgtAgeLmtYn),

            // 소득
            earnCondition = policy.earnCndSeCd,
            earnMin = parseInteger(policy.earnMinAmt),
            earnMax = parseInteger(policy.earnMaxAmt),

            // 대상 조건
            regionCode = policy.zipCd,
            jobCode = policy.jobCd,
            schoolCode = policy.schoolCd,
            marriageStatus = policy.mrgSttsCd,

            // 태그 / 분류
            keywords = parseKeywords(policy.plcyKywdNm),
            specialBizCode = policy.sBizCd,

            // 검색용 텍스트
            description = buildDescription(policy.plcyExplnCn, policy.plcySprtCn)
        )
    }

    /* ===== 유틸 메서드 ===== */
    private fun parseInteger(value: String?): Int? {
        try {
            return if (value == null || value.isBlank()) null else value.toInt()
        } catch (e: NumberFormatException) {
            return null
        }
    }

    private fun parseBoolean(value: String?): Boolean? {
        if (value == null) return null
        return "Y".equals(value, ignoreCase = true)
    }

    private fun parseKeywords(keywords: String?): MutableList<String?> {
        if (keywords == null || keywords.isBlank()) {
            return mutableListOf<String?>()
        }
        // 예: "청년,주거,취업"
        return Arrays.stream<String>(keywords.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .map<String?> { obj: String? -> obj!!.trim { it <= ' ' } }
            .filter { s: String? -> !s!!.isEmpty() }
            .toList()
    }

    private fun buildDescription(vararg texts: String?): String? {
        return Arrays.stream<String?>(texts)
            .filter { t: String? -> t != null && !t.isBlank() }
            .reduce { a: String?, b: String? -> a + " " + b }
            .orElse(null)
    }
}
