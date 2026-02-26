package com.back.domain.auth.service

import com.back.domain.auth.repository.RefreshTokenRepository
import com.back.domain.auth.util.TokenHasher
import com.back.global.exception.ServiceException
import com.back.global.security.jwt.JwtProvider
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
@Transactional
class AuthServiceDB(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider
) {

    companion object {
        private val ACCESS_MAX_AGE: Duration = Duration.ofMinutes(20)
    }

    // refreshToken 쿠키를 검증해서 새 accessToken 쿠키 반환
    @Transactional(readOnly = true)
    fun reissueAccessTokenCookie(request: HttpServletRequest): String {
        // 요청에서 리프레시 토큰 꺼내기
        val rawRefreshToken = getCookieValue(request, "refreshToken")
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw ServiceException("AUTH-401", "refreshToken 쿠키가 없습니다.")
        }

        // 해시로 변환
        val hash = TokenHasher.sha256Hex(rawRefreshToken)

        // 해시로 디비 조회
        val refreshToken = refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow { ServiceException("AUTH-401", "유효하지 않은 refresh token 입니다.") }

        val now = LocalDateTime.now()
        if (refreshToken.isRevoked()) {
            throw ServiceException("AUTH-401", "폐기된 refresh token 입니다.")
        }
        if (refreshToken.isExpired()) {
            throw ServiceException("AUTH-401", "만료된 refresh token 입니다.")
        }

        // 회원 정보 꺼내서 새 AccessToken 발급
        val member = refreshToken.member
        val newAccessToken =
            jwtProvider.issueAccessToken(member.id!!, member.role.toString())

        // 새 access 토큰 반환
        return buildAccessCookieHeader(newAccessToken)
    }

    // 요청에서 리프레시 토큰 꺼내기
    private fun getCookieValue(request: HttpServletRequest, name: String): String? {
        val cookies: Array<Cookie> = request.cookies ?: return null

        for (c in cookies) {
            if (name == c.name) {
                return c.value
            }
        }
        return null
    }

    // 로그인이랑 같은 토큰 생성
    private fun buildAccessCookieHeader(token: String): String {
        return ResponseCookie.from("accessToken", token)
            .httpOnly(true)
            .secure(false) // dev
            .path("/")
            .sameSite("Lax")
            .maxAge(ACCESS_MAX_AGE)
            .build()
            .toString()
    }
}
