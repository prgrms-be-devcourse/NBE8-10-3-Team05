package com.back.domain.member.bookmark.controller

import com.back.domain.member.bookmark.entity.Bookmark
import com.back.domain.member.bookmark.repository.BookmarkRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1BookmarkControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var policyRepository: PolicyRepository

    @Autowired
    private lateinit var bookmarkRepository: BookmarkRepository

    @Test
    @DisplayName("북마크 목록 조회 성공 - 북마크가 있는 경우 200 + BookmarkPolicyResponseDto 반환")
    @Throws(Exception::class)
    fun bookmarksSuccessWithItemsTest() {
            // given: Member 생성 및 저장
            val member =
                Member.createEmailUser(
                    "홍길동",
                    "test@example.com",
                    "encodedPassword123",
                    "991231",
                    "1"
                )
            val saved = memberRepository.save(member)

            // given: Policy 생성 및 저장
            val policy =
                createTestPolicy("BOOKMARK-POLICY-001", "북마크 테스트 정책")
            val savedPolicy =
                policyRepository.save(policy)

            // given: Bookmark 생성 및 저장
            val bookmark = createTestBookmark(saved, savedPolicy)
            bookmarkRepository.save(bookmark)

            // when & then: GET 요청 보내고 정상적인 응답 확인
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/member/bookmark/welfare-bookmarks")
                    .with(
                        SecurityMockMvcRequestPostProcessors.authentication(
                            UsernamePasswordAuthenticationToken(
                                getMemberId(saved),
                                null,
                                listOf(SimpleGrantedAuthority("ROLE_USER"))
                            )
                        )
                    )
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                    MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.policies").isArray())
                .andExpect(
                    MockMvcResultMatchers.jsonPath(
                        "$.policies",
                        Matchers.hasSize<Any>(1)  // <Any> 추가
                    )
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.policies[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.policies[0].plcyNo").value("BOOKMARK-POLICY-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.policies[0].plcyNm").value("북마크 테스트 정책"))
                .andDo(MockMvcResultHandlers.print())
        }

    @Test
    @DisplayName("북마크 목록 조회 성공 - 북마크가 없는 경우 200 + 빈 리스트 반환")
    @Throws(Exception::class)
    fun bookmarksSuccessEmptyTest() {
        // given: Member 생성 및 저장
        val member =
            Member.createEmailUser(
                "홍길동",
                "test@example.com",
                "encodedPassword123",
                "991231",
                "1"
            )
        val saved = memberRepository.save(member)

        // when & then: GET 요청 보내고 빈 리스트 반환 확인
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/member/bookmark/welfare-bookmarks")
                .with(
                    SecurityMockMvcRequestPostProcessors.authentication(
                        UsernamePasswordAuthenticationToken(
                            getMemberId(saved),
                            null,
                            listOf(SimpleGrantedAuthority("ROLE_USER"))
                        )
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                    MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.policies").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.policies").isEmpty())
        }

    @Test
    @DisplayName("북마크 목록 조회 실패 - JWT 토큰이 없는 경우 401 반환")
    @Throws(Exception::class)
    fun bookmarksFailNoJwtTest() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/member/bookmark/welfare-bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("AUTH-401"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("인증 정보가 없습니다."))
        }

    @Test
    @DisplayName("북마크 추가 성공 - 200 + BookmarkUpdateResponseDto 반환")
    @Throws(Exception::class)
    fun updateBookmarkAddSuccessTest() {
        // given: Member 생성 및 저장
        val member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1")
        val saved = memberRepository.save(member)

        // given: Policy 생성 및 저장
        val policy = createTestPolicy("BOOKMARK-ADD-001", "북마크 추가 테스트 정책")
        val savedPolicy = policyRepository.save(policy)

        // when & then: POST 요청 (북마크 추가) 후 정상 응답 확인
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/member/bookmark/welfare-bookmarks/${savedPolicy.id}")
                .with(
                    SecurityMockMvcRequestPostProcessors.authentication(
                        UsernamePasswordAuthenticationToken(
                            getMemberId(saved), null, listOf(SimpleGrantedAuthority("ROLE_USER"))
                        )
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("북마크가 추가되었습니다."))
    }

    @Test
    @DisplayName("북마크 해제 성공 - 200 + BookmarkUpdateResponseDto 반환")
    @Throws(Exception::class)
    fun updateBookmarkRemoveSuccessTest() {
        // given: Member 생성 및 저장
        val member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1")
        val saved = memberRepository.save(member)

        // given: Policy 생성 및 저장
        val policy = createTestPolicy("BOOKMARK-REMOVE-001", "북마크 해제 테스트 정책")
        val savedPolicy = policyRepository.save(policy)

        // given: Bookmark 생성 및 저장 (추가된 상태)
        val bookmark = createTestBookmark(saved, savedPolicy)
        bookmarkRepository.save(bookmark)

        // when & then: POST 요청 (북마크 해제) 후 정상 응답 확인
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/member/bookmark/welfare-bookmarks/${savedPolicy.id}")
                .param("policyId", savedPolicy.id.toString())
                .with(
                    SecurityMockMvcRequestPostProcessors.authentication(
                        UsernamePasswordAuthenticationToken(
                            getMemberId(saved), null, listOf(SimpleGrantedAuthority("ROLE_USER"))
                        )
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("북마크가 해제되었습니다."))
    }

    @Test
    @DisplayName("북마크 추가/해제 실패 - JWT 토큰이 없는 경우 401 반환")
    @Throws(Exception::class)
    fun updateBookmarkFailNoJwtTest() {
        val policy = createTestPolicy("BOOKMARK-401-001", "인증 실패 테스트 정책")
        val savedPolicy = policyRepository.save(policy)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/member/bookmark/welfare-bookmarks/${savedPolicy.id}")
                .param("policyId", savedPolicy.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("AUTH-401"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("인증 정보가 없습니다."))
    }

    @Test
    @DisplayName("북마크 추가/해제 실패 - 존재하지 않는 Policy ID 404 반환")
    @Throws(Exception::class)
    fun updateBookmarkFailPolicyNotFoundTest() {
        val member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1")
        val saved = memberRepository.save(member)

        val nonExistentPolicyId = 99999

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/member/bookmark/welfare-bookmarks/$nonExistentPolicyId")
                .param("policyId", nonExistentPolicyId.toString())
                .with(
                    SecurityMockMvcRequestPostProcessors.authentication(
                        UsernamePasswordAuthenticationToken(
                            getMemberId(saved), null, listOf(SimpleGrantedAuthority("ROLE_USER"))
                        )
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    private fun createTestPolicy(plcyNo: String?, plcyNm: String?): Policy {
        val policy = Policy() // 기본 생성자
        val fields = mapOf(
            "plcyNo" to (plcyNo ?: ""),
            "plcyNm" to (plcyNm ?: ""),
            "plcyKywdNm" to "",
            "plcyExplnCn" to ""
        )
        fields.forEach { (name, value) ->
            Policy::class.java.getDeclaredField(name).apply {
                isAccessible = true
                set(policy, value)
            }
        }
        return policy
    }

    private fun createTestBookmark(applicant: Member?, policy: Policy?): Bookmark {
        return Bookmark(applicant = applicant, policy = policy)
    }

    private fun getMemberId(member: Member): Long {
        // Member의 id는 internal이므로 리플렉션을 사용하여 접근
        return try {
            val idField = Member::class.java.getDeclaredField("id")
            idField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            (idField.get(member) as? Long) ?: throw IllegalStateException("Member id가 null입니다")
        } catch (e: Exception) {
            throw RuntimeException("Member id 접근 실패", e)
        }
    }
}
