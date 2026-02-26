package com.back.domain.welfare.policy.enumtype

import com.back.global.enumtype.CodeEnum
import lombok.Getter
import lombok.RequiredArgsConstructor

// 신청기간 구분코드
enum class ApplyPeriodType(
    override val code: String,
    override val description: String
) : CodeEnum {

    FIXED_PERIOD("057001", "특정기간"),
    ALWAYS("057002", "상시"),
    CLOSED("057003", "마감");

    companion object {
        fun fromCode(code: String): ApplyPeriodType? =
            entries.find { it.code == code }
    }
}
