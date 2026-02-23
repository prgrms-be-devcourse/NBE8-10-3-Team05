package com.back.domain.member.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.geo.entity.Address;
import com.back.domain.member.member.dto.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberDetailService;
import com.back.domain.member.member.service.MemberService;
import com.back.standard.util.ActorProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberDetailService memberDetailService;
    private final ActorProvider actorProvider;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@RequestBody JoinRequest req) {
        JoinResponse res = memberService.join(req);
        return ResponseEntity.ok(res);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse res = memberService.login(req, response);
        return ResponseEntity.ok(res);
    }

    // 보호 API: 토큰 있어야만 접근 가능하게 만들 거임
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(memberService.me());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        MemberService.LogoutCookieHeaders headers = memberService.logout(request);

        // TODO: 이 로직은 service안에 들어가야 할 것 같습니다. 지금까지의 코드를 보면.
        response.addHeader("Set-Cookie", headers.deleteAccessCookieHeader());
        response.addHeader("Set-Cookie", headers.deleteRefreshCookieHeader());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete-social")
    public ResponseEntity<Void> completeSocial(@RequestBody CompleteSocialSignupRequest req) {
        memberService.completeSocialSignup(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/detail")
    public ResponseEntity<MemberDetailRes> getMemberDetail() {
        Member actor = actorProvider.getActor();

        MemberDetailRes response = memberDetailService.getDetail(actor.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/detail")
    public ResponseEntity<MemberDetailRes> modifyMemberDetail(@Valid @RequestBody MemberDetailReq reqBody) {

        Member actor = actorProvider.getActor();

        memberDetailService.modify(actor.getId(), reqBody);
        MemberDetailRes response = memberDetailService.getDetail(actor.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/detail/address")
    public ResponseEntity<MemberDetailRes> updateAddress(@Valid @RequestBody Address address) {

        Member actor = actorProvider.getActor();

        memberDetailService.updateAddress(actor.getId(), address);
        MemberDetailRes response = memberDetailService.getDetail(actor.getId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> withdraw(HttpServletResponse response) {
        memberService.withdraw(response);
        return ResponseEntity.noContent().build(); // 204
    }
}
