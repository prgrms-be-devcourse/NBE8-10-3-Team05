package com.back.domain.member.bookmark.controller

import com.back.domain.member.bookmark.dto.BookmarkPolicyResponseDto
import com.back.domain.member.bookmark.dto.BookmarkUpdateResponseDto
import com.back.domain.member.bookmark.service.BookmarkService
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.standard.util.ActorProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/member/bookmark")
class BookmarkController(                          // ✅ 생성자 주입
    private val bookmarkService: BookmarkService,
    private val policyRepository: PolicyRepository,
    private val actorProvider: ActorProvider
) {

    @GetMapping("/welfare-bookmarks")
    fun getBookmarks(): ResponseEntity<BookmarkPolicyResponseDto> {
        val member = actorProvider.getActor()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BookmarkPolicyResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요", null))

        val policies = bookmarkService.getPolicies(member)
        return ResponseEntity.ok(BookmarkPolicyResponseDto(200, "", policies.toMutableList()))
    }

    @PostMapping("/welfare-bookmarks/{policyId}")
    fun updateBookmark(@PathVariable policyId: Int): ResponseEntity<BookmarkUpdateResponseDto> {  // ✅ Int → Long
        val member = actorProvider.getActor()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val policy = policyRepository.findById(policyId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val message = bookmarkService.changeBookmarkStatus(member, policy)
        return ResponseEntity.ok(BookmarkUpdateResponseDto(200, message))
    }
}
