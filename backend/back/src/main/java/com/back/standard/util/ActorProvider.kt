package com.back.standard.util

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ActorProvider(
    private val memberRepository: MemberRepository
) {

    @Transactional(readOnly = true)
    fun getActor(): Member {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth == null ||
            !auth.isAuthenticated ||
            auth.principal == null ||
            auth.principal == "anonymousUser"
        ) {
            throw ServiceException("AUTH-401", "인증 정보가 없습니다.")
        }

        val memberId: Long = try {
            // principal에 memberId를 넣어둔 상태라서 이렇게 꺼내면 됨
            auth.principal as Long
        } catch (e: ClassCastException) {
            // 혹시 String으로 들어오는 경우 대비
            auth.principal.toString().toLong()
        }

        // get actor같은 연할을 하는 곳인데 강사님은 DB조회를 안하고 나는 DB조희를 함
        return memberRepository
            .findById(memberId)
            .orElseThrow { ServiceException("MEMBER-404", "존재하지 않는 회원입니다.") }
    }
}
