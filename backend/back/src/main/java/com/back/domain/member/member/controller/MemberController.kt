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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/member/member")
class MemberController(
    private val memberService: MemberService,
) {

    // 회원가입
    @PostMapping("/join")
    fun join(@RequestBody req: JoinRequest): ResponseEntity<JoinResponse> {
        val res = memberService.join(req)
        return ResponseEntity.ok(res)
    }

    // 로그인
    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginRequest,
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

        val headers = memberService.logout(request)

        // TODO: 이 로직은 service안에 들어가야 할 것 같습니다. 지금까지의 코드를 보면.
        response.addHeader("Set-Cookie", headers.deleteAccessCookieHeader)
        response.addHeader("Set-Cookie", headers.deleteRefreshCookieHeader)

        return ResponseEntity.ok().build()
    }

    @PostMapping("/complete-social")
    fun completeSocial(@RequestBody req: CompleteSocialSignupRequest): ResponseEntity<Void> {
        memberService.completeSocialSignup(req)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/delete")
    fun withdraw(response: HttpServletResponse): ResponseEntity<Void> {
        memberService.withdraw(response)
        return ResponseEntity.noContent().build() // 204
    }
}
