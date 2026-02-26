package com.back.domain.welfare.policy.enumtype

import com.back.global.enumtype.CodeEnum
import lombok.Getter
import lombok.RequiredArgsConstructor

// 정책 특화요건 코드
enum class SpecialPolicyRequirement(
    override val code: String,
    override val description: String
) : CodeEnum {
    SME("014001", "중소기업"),
    WOMEN("014002", "여성"),
    BASIC_LIVELIHOOD("014003", "기초생활수급자"),
    SINGLE_PARENT("014004", "한부모가정"),
    DISABLED("014005", "장애인"),
    FARMER("014006", "농업인"),
    SOLDIER("014007", "군인"),
    LOCAL_TALENT("014008", "지역인재"),
    ETC("014009", "기타"),
    NO_LIMIT("014010", "제한없음");
}
