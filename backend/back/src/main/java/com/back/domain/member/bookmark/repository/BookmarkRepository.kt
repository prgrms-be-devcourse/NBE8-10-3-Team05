package com.back.domain.member.bookmark.repository

import com.back.domain.member.bookmark.entity.Bookmark
import com.back.domain.member.member.entity.Member
import com.back.domain.welfare.policy.entity.Policy
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun getBookmarksByApplicantId(applicantId: Long?): MutableList<Bookmark?>?

    fun findByApplicantAndPolicy(member: Member?, policy: Policy?): Optional<Bookmark?>?
}
