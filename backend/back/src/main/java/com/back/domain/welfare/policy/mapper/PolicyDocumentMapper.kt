package com.back.domain.welfare.policy.mapper

import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.entity.Policy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PolicyDocumentMapper {

    companion object {
        private val log = LoggerFactory.getLogger(PolicyDocumentMapper::class.java)
    }

    fun toDocument(policy: Policy): PolicyDocument? {
        return PolicyDocument.builder()
            .policyId(policy.id)                          // ✅ getId() → .id
            .plcyNo(policy.plcyNo)                        // ✅ getPlcyNo() → .plcyNo
            .plcyNm(policy.plcyNm)

            .minAge(parseInteger(policy.sprtTrgtMinAge))
            .maxAge(parseInteger(policy.sprtTrgtMaxAge))
            .ageLimited(parseBoolean(policy.sprtTrgtAgeLmtYn))

            .earnCondition(policy.earnCndSeCd)
            .earnMin(parseInteger(policy.earnMinAmt))
            .earnMax(parseInteger(policy.earnMaxAmt))

            .regionCode(policy.zipCd)
            .jobCode(policy.jobCd)
            .schoolCode(policy.schoolCd)
            .marriageStatus(policy.mrgSttsCd)

            .keywords(parseKeywords(policy.plcyKywdNm))
            .specialBizCode(policy.sBizCd)

            .description(buildDescription(policy.plcyExplnCn, policy.plcySprtCn))
            .build()
    }

    private fun parseInteger(value: String?): Int? {
        return try {
            if (value.isNullOrBlank()) null else value.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun parseBoolean(value: String?): Boolean? {
        return value?.equals("Y", ignoreCase = true)
    }

    private fun parseKeywords(keywords: String?): MutableList<String?> {
        if (keywords.isNullOrBlank()) return mutableListOf()
        return keywords.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()
    }

    private fun buildDescription(vararg texts: String?): String? {
        return texts.filterNotNull()
            .filter { it.isNotBlank() }
            .reduceOrNull { a, b -> "$a $b" }
    }
}
