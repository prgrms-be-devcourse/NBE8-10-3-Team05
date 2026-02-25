package com.back.domain.auth.repository

import com.back.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    // refresh token hash로 조회 (reissue에서 사용)
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>

    // 로그아웃 시 회원의 refresh 토큰 전부 삭제/폐기용
    fun deleteByMember_Id(memberId: Long)
}
