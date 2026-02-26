package com.back.domain.member.member.service

import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AuthCookieService {

    fun accessCookie(token: String, maxAge: Duration): String =
        ResponseCookie.from("accessToken", token)
            .httpOnly(true)
            .secure(false) // TODO: 추후 true로 변경
            .path("/")
            .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
            .maxAge(maxAge)
            .build()
            .toString()

    fun refreshCookie(raw: String, maxAge: Duration): String =
        ResponseCookie.from("refreshToken", raw)
            .httpOnly(true)
            .secure(false) // TODO: 추후 true로 변경
            .path("/api/v1/auth/reissue")
            .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
            .maxAge(maxAge)
            .build()
            .toString()

    fun deleteCookie(name: String): String {
        if (name == "refreshToken") {
            return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth/reissue")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build()
                .toString()
        }

        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(false) // TODO: 추후 true로 변경
            .path("/")
            .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
            .maxAge(Duration.ZERO)
            .build()
            .toString()
    }
}
