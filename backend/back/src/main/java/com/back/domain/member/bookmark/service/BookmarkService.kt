package com.back.domain.member.bookmark.service

import com.back.domain.member.bookmark.repository.BookmarkRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.member.bookmark.entity.Bookmark
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository
) {

    fun getPolicies(member: Member): List<Policy> {
        // member.id가 private이므로 reflection 사용
        val memberId = Member::class.java.getDeclaredField("id")
            .apply { isAccessible = true }
            .get(member) as Long

        return bookmarkRepository.getBookmarksByApplicantId(memberId)
            ?.mapNotNull { it?.policy }
            ?: emptyList()
    }

    @Transactional
    fun changeBookmarkStatus(member: Member, policy: Policy): String {
        val existingBookmark = bookmarkRepository.findByApplicantAndPolicy(member, policy)

        return if (existingBookmark?.isPresent == true) {
            bookmarkRepository.delete(existingBookmark.get())
            "북마크가 해제되었습니다."
        } else {
            bookmarkRepository.save(Bookmark(policy, member))
            "북마크가 추가되었습니다."
        }
    }
}
