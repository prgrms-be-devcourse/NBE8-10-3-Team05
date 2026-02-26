package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {

    // 로그인/회원가입에서 이메일로 회원 찾기
    fun findByEmail(email: String): Optional<Member>

    // 회원가입에서 이메일 중복 체크
    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndStatus(email: String?, status: Member.MemberStatus): Boolean

    fun findByTypeAndProviderId(type: Member.LoginType, providerId: String): Optional<Member>
}
