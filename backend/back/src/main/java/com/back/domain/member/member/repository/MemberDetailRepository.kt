package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.MemberDetail
import org.springframework.data.jpa.repository.JpaRepository

interface MemberDetailRepository : JpaRepository<MemberDetail, Long>
