package com.back.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.auth.service.AuthServiceRedis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServiceRedis authService;

    // 리프레시토큰 쿠키 검증해서 새 쿠키 내려주기
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        authService.reissueAccessTokenCookie(request, response);
        return ResponseEntity.ok().build();
    }
}
