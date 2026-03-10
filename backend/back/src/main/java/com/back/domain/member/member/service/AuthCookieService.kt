package com.back.domain.member.member.service

import org.springframework.util.SerializationUtils
import java.util.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Duration

//vercel에서 ec2로 토큰을 전송하기 위해 COOKIE_SECURE=true, COOKIE_SAME_SITE=None 설정해야함
//기존 코드가 최대한 유지되도록 yml설정값으로 변경하고, yml기본값은 기존대로 false, Lax 설정
//vercel -> ec2 환경에서는 백엔드 배포 시 환경변수 설정 필요 (true, None)
@Service
class AuthCookieService(
    @Value("\${custom.cookie.secure}")    private val secureCookie: Boolean,
    @Value("\${custom.cookie.same-site}") private val sameSite: String
) {


    fun accessCookie(token: String, maxAge: Duration): String =
        ResponseCookie.from("accessToken", token)
            .httpOnly(true)
            .secure(secureCookie)
            .path("/")
            .sameSite(sameSite)
            .maxAge(maxAge)
            .build()
            .toString()

    fun refreshCookie(raw: String, maxAge: Duration): String =
        ResponseCookie.from("refreshToken", raw)
            .httpOnly(true)
            .secure(secureCookie)
            .path("/api/v1/auth/reissue")
            .sameSite(sameSite)
            .maxAge(maxAge)
            .build()
            .toString()

    fun deleteCookie(name: String): String {
        if (name == "refreshToken") {
            return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/v1/auth/reissue")
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build()
                .toString()
        }

        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(secureCookie)
            .path("/")
            .sameSite(sameSite)
            .maxAge(Duration.ZERO)
            .build()
            .toString()
    }

    // --- 객체 직렬화 유틸리티 (내부용) ---
    fun serialize(obj: Any): String {
        return Base64.getUrlEncoder()
            .encodeToString(SerializationUtils.serialize(obj))
    }

    fun <T> deserialize(cookieValue: String, cls: Class<T>): T? {
        return cls.cast(
            SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookieValue)
            )
        )
    }

    fun createCookie(name: String, value: String, maxAgeSeconds: Int): String =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secureCookie)
            .path("/")
            .sameSite(sameSite)
            .maxAge(maxAgeSeconds.toLong())
            .build()
            .toString()
}
