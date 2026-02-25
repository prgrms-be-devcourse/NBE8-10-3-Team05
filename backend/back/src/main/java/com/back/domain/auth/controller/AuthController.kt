package com.back.domain.auth.controller

import com.back.domain.auth.service.AuthServiceRedis
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthServiceRedis
) {

    // 리프레시토큰 쿠키 검증해서 새 쿠키 내려주기
    @PostMapping("/reissue")
    fun reissue(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Void> {
        authService.reissueAccessTokenCookie(request, response)
        return ResponseEntity.ok().build()
    }
}
