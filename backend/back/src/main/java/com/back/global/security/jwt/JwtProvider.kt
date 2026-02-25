package com.back.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    props: JwtProperties
) {

    private val key: SecretKey =
        Keys.hmacShaKeyFor(
            props.jwt.secretKey.toByteArray(StandardCharsets.UTF_8)
        )

    private val accessTokenExpSeconds: Long =
        props.accessToken.expirationSeconds

    fun issueAccessToken(
        memberId: Long,
        role: String
    ): String {

        val now = Instant.now()
        val exp = now.plusSeconds(accessTokenExpSeconds)

        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("role", role)
            .signWith(key)
            .compact()
    }

    /**
     * 토큰 검증 + Claims 추출
     * - 서명 검증
     * - 만료 검증(exp)
     * - 문제가 있으면 예외 발생
     */
    fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
