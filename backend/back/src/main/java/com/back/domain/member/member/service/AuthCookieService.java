package com.back.domain.member.member.service;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public String accessCookie(String token, Duration maxAge) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // TODO: 추후 true로 변경
                .path("/")
                .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public String refreshCookie(String raw, Duration maxAge) {
        return ResponseCookie.from("refreshToken", raw)
                .httpOnly(true)
                .secure(false) // TODO: 추후 true로 변경
                .path("/api/v1/auth/reissue")
                .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public String deleteCookie(String name) {
        if (name.equals("refreshToken")) {
            return ResponseCookie.from(name, "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/api/v1/auth/reissue")
                    .sameSite("Lax")
                    .maxAge(Duration.ZERO)
                    .build()
                    .toString();
        }
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false) // TODO: 추후 true로 변경
                .path("/")
                .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }
}
