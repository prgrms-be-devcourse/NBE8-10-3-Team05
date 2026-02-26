package com.back.global.enumtype

import lombok.Getter
import lombok.RequiredArgsConstructor

enum class MarriageStatus(
    override val code: String,
    override val description: String
) : CodeEnum {
    MARRIED("055001", "기혼"),
    SINGLE("055002", "미혼");
}
