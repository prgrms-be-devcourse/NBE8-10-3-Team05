package com.back.global.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom")
data class JwtProperties(
    val jwt: Jwt,
    val accessToken: AccessToken
) {
    data class Jwt(
        // JWT 서명 비밀키
        val secretKey: String
    )

    data class AccessToken(
        // Access Token 만료 시간 (초 단위)
        val expirationSeconds: Long
    )
}
