package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.CompleteSocialSignupRequest
import com.back.domain.member.member.dto.JoinRequest
import com.back.domain.member.member.dto.JoinResponse
import com.back.domain.member.member.dto.LoginRequest
import com.back.domain.member.member.dto.LoginResponse
import com.back.domain.member.member.dto.MeResponse
import com.back.domain.member.member.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/member/member")
class MemberController(
    private val memberService: MemberService,
) {

    // 회원가입
    @PostMapping("/join")
    fun join(@Valid @RequestBody req: JoinRequest): ResponseEntity<JoinResponse> {
        val res = memberService.join(req)
        return ResponseEntity.ok(res)
    }

    // 로그인
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody req: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val res = memberService.login(req, response)
        return ResponseEntity.ok(res)
    }

    // 보호 API: 토큰 있어야만 접근 가능하게 만들 거임
    @GetMapping("/me")
    fun me(): ResponseEntity<MeResponse> {
        return ResponseEntity.ok(memberService.me())
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Void> {
        memberService.logout(request, response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/complete-social")
    fun completeSocial(@Valid @RequestBody req: CompleteSocialSignupRequest): ResponseEntity<Void> {
        memberService.completeSocialSignup(req)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/delete")
    fun withdraw(response: HttpServletResponse): ResponseEntity<Void> {
        memberService.withdraw(response)
        return ResponseEntity.noContent().build() // 204
    }
}
