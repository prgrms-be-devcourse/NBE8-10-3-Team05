package com.back.standard.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActorProvider {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ServiceException("AUTH-401", "인증 정보가 없습니다.");
        }

        Long memberId;
        try {
            // principal에 memberId를 넣어둔 상태라서 이렇게 꺼내면 됨
            memberId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            // 혹시 String으로 들어오는 경우 대비
            memberId = Long.valueOf(String.valueOf(auth.getPrincipal()));
        }

        // get actor같은 연할을 하는 곳인데 강사님은 DB조회를 안하고 나는 DB조희를 함
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));
    }
}
