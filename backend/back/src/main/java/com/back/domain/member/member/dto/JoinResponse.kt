package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import java.time.LocalDateTime

// 회원가입 성공 시 프론트에 내려줄 응답 형태
data class JoinResponse(
    val id: Long,
    val name: String,
    val email: String?,
    val type: Member.LoginType,
    val role: Member.Role,
    val createdAt: LocalDateTime?
) {
    companion object {
        // 멤버 엔티티 → 응답 DTO 변환
        fun from(member: Member): JoinResponse {
            return JoinResponse(
                id = requireNotNull(member.id),
                name = member.name,
                email = member.email,
                type = member.type,
                role = member.role,
                createdAt = member.createdAt
            )
        }
    }
}
