package com.back.domain.welfare.policy.enumtype

import com.back.global.enumtype.CodeEnum
import lombok.Getter
import lombok.RequiredArgsConstructor

// 결혼상태코드
enum class MarriageRequirement(
    override val code: String,
    override val description: String
) : CodeEnum {
    MARRIED("055001", "기혼"),
    SINGLE("055002", "미혼"),
    NO_LIMIT("055003", "제한없음");
}
