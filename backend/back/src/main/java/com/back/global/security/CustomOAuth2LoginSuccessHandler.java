package com.back.global.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.security.jwt.JwtProvider;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long memberId = Optional.ofNullable(oAuth2User.getAttribute("memberId"))
                .map(obj -> Long.valueOf(String.valueOf(obj)))
                .orElseThrow(() -> new IllegalStateException("OAuth2 세션에 memberId가 없습니다."));

        Member.Role memberRole = Optional.ofNullable(oAuth2User.getAttribute("memberRole"))
                .map(obj -> Member.Role.valueOf(String.valueOf(obj)))
                .orElse(Member.Role.USER); // 기본값 유저

        Member.MemberStatus memberStatus = Optional.ofNullable(oAuth2User.getAttribute("memberStatus"))
                .map(obj -> Member.MemberStatus.valueOf(String.valueOf(obj)))
                .orElse(Member.MemberStatus.PRE_REGISTERED); // 여기는 socialLogin하는 곳이니 기본값 pre_

        // 일반 로그인과 동일한 쿠키 발급 (access + refresh)
        memberService.issueLoginCookiesWithoutMemberEntity(memberId, memberRole, response);

        // PRE_REGISTERED 상태면 추가정보 입력 페이지로, ACTIVE면 메인으로
        if (memberStatus == Member.MemberStatus.PRE_REGISTERED) {
            response.sendRedirect("http://localhost:3000/social-signup");
        } else {
            response.sendRedirect("http://localhost:3000");
        }
    }
}
