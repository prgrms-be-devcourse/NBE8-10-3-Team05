package com.back.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpSeconds;

    public JwtProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.jwt().secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpSeconds = props.accessToken().expirationSeconds();
    }

    public String issueAccessToken(long memberId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenExpSeconds);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", email)
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    public String issueAccessTokenWithoutEmail(long memberId, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenExpSeconds);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    // 토큰 검증 + Claims 추출
    // 서명 검증
    // 만료 검증(exp)
    // 문제가 있으면 예외 터짐
    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
