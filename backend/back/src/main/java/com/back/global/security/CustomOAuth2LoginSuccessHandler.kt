package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.security.jwt.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomOAuth2LoginSuccessHandler(
    private val memberService: MemberService,
    private val jwtProvider: JwtProvider
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User

        val memberId: Long = oAuth2User.getAttribute<Any>("memberId")
            ?.let { it.toString().toLong() }
            ?: throw IllegalStateException("OAuth2 세션에 memberId가 없습니다.")

        val memberRole: Member.Role =
            oAuth2User.getAttribute<Any>("memberRole")
                ?.let { Member.Role.valueOf(it.toString()) }
                ?: Member.Role.USER // 기본값 유저

        val memberStatus: Member.MemberStatus =
            oAuth2User.getAttribute<Any>("memberStatus")
                ?.let { Member.MemberStatus.valueOf(it.toString()) }
                ?: Member.MemberStatus.PRE_REGISTERED // 여기는 socialLogin하는 곳이니 기본값 pre_

        // 일반 로그인과 동일한 쿠키 발급 (access + refresh)
        memberService.issueLoginCookiesWithoutMemberEntity(memberId, memberRole, response)

        // PRE_REGISTERED 상태면 추가정보 입력 페이지로, ACTIVE면 메인으로
        if (memberStatus == Member.MemberStatus.PRE_REGISTERED) {
            response.sendRedirect("http://localhost:3000/social-signup")
        } else {
            response.sendRedirect("http://localhost:3000")
        }
    }
}
