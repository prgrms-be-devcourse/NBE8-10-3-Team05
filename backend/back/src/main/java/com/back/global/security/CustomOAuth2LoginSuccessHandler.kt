package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.security.jwt.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomOAuth2LoginSuccessHandler(
    private val memberService: MemberService,
    private val jwtProvider: JwtProvider,
    @Value("\${app.frontend-url:http://localhost:3000}") //초기값 localhost(기존코드)
    private val frontendUrl: String
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
                ?: Member.Role.USER

        val memberStatus: Member.MemberStatus =
            oAuth2User.getAttribute<Any>("memberStatus")
                ?.let { Member.MemberStatus.valueOf(it.toString()) }
                ?: Member.MemberStatus.PRE_REGISTERED

        memberService.issueLoginCookiesWithoutMemberEntity(memberId, memberRole, response)

        if (memberStatus == Member.MemberStatus.PRE_REGISTERED) {
            response.sendRedirect("$frontendUrl/social-signup")
        } else {
            response.sendRedirect(frontendUrl)
        }
    }
}
