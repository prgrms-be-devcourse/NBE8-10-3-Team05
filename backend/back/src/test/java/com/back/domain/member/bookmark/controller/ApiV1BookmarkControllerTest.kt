package com.back.domain.member.bookmark.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.bookmark.entity.Bookmark;
import com.back.domain.member.bookmark.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    @DisplayName("북마크 목록 조회 성공 - 북마크가 있는 경우 200 + BookmarkPolicyResponseDto 반환")
    void getBookmarksSuccessWithItemsTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1");
        Member saved = memberRepository.save(member);

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("BOOKMARK-POLICY-001", "북마크 테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // given: Bookmark 생성 및 저장
        Bookmark bookmark = createTestBookmark(saved, savedPolicy);
        bookmarkRepository.save(bookmark);

        // when & then: GET 요청 보내고 정상적인 응답 확인
        mockMvc.perform(get("/api/v1/member/bookmark/welfare-bookmarks")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                saved.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(""))
                .andExpect(jsonPath("$.policies").isArray())
                .andExpect(jsonPath("$.policies", hasSize(1)))
                .andExpect(jsonPath("$.policies[0].id").exists())
                .andExpect(jsonPath("$.policies[0].plcyNo").value("BOOKMARK-POLICY-001"))
                .andExpect(jsonPath("$.policies[0].plcyNm").value("북마크 테스트 정책"))
                .andDo(print());
    }

    @Test
    @DisplayName("북마크 목록 조회 성공 - 북마크가 없는 경우 200 + 빈 리스트 반환")
    void getBookmarksSuccessEmptyTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1");
        Member saved = memberRepository.save(member);

        // when & then: GET 요청 보내고 빈 리스트 반환 확인
        mockMvc.perform(get("/api/v1/member/bookmark/welfare-bookmarks")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                saved.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(""))
                .andExpect(jsonPath("$.policies").isArray())
                .andExpect(jsonPath("$.policies").isEmpty());
    }

    @Test
    @DisplayName("북마크 목록 조회 실패 - JWT 토큰이 없는 경우 401 반환")
    void getBookmarksFailNoJwtTest() throws Exception {
        mockMvc.perform(get("/api/v1/member/bookmark/welfare-bookmarks").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("AUTH-401"))
                .andExpect(jsonPath("$.msg").value("인증 정보가 없습니다."));
    }

    @Test
    @DisplayName("북마크 추가 성공 - 200 + BookmarkUpdateResponseDto 반환")
    void updateBookmarkAddSuccessTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1");
        Member saved = memberRepository.save(member);

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("BOOKMARK-ADD-001", "북마크 추가 테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // when & then: POST 요청 (북마크 추가) 후 정상 응답 확인
        mockMvc.perform(post("/api/v1/member/bookmark/welfare-bookmarks/" + savedPolicy.getId())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                saved.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("북마크가 추가되었습니다."));
    }

    @Test
    @DisplayName("북마크 해제 성공 - 200 + BookmarkUpdateResponseDto 반환")
    void updateBookmarkRemoveSuccessTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1");
        Member saved = memberRepository.save(member);

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("BOOKMARK-REMOVE-001", "북마크 해제 테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // given: Bookmark 생성 및 저장 (추가된 상태)
        Bookmark bookmark = createTestBookmark(saved, savedPolicy);
        bookmarkRepository.save(bookmark);

        // when & then: POST 요청 (북마크 해제) 후 정상 응답 확인
        mockMvc.perform(post("/api/v1/member/bookmark/welfare-bookmarks/" + savedPolicy.getId())
                        .param("policyId", String.valueOf(savedPolicy.getId()))
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                saved.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("북마크가 해제되었습니다."));
    }

    @Test
    @DisplayName("북마크 추가/해제 실패 - JWT 토큰이 없는 경우 401 반환")
    void updateBookmarkFailNoJwtTest() throws Exception {
        Policy policy = createTestPolicy("BOOKMARK-401-001", "인증 실패 테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        mockMvc.perform(post("/api/v1/member/bookmark/welfare-bookmarks/" + savedPolicy.getId())
                        .param("policyId", String.valueOf(savedPolicy.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("AUTH-401"))
                .andExpect(jsonPath("$.msg").value("인증 정보가 없습니다."));
    }

    @Test
    @DisplayName("북마크 추가/해제 실패 - 존재하지 않는 Policy ID 404 반환")
    void updateBookmarkFailPolicyNotFoundTest() throws Exception {
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", "991231", "1");
        Member saved = memberRepository.save(member);

        int nonExistentPolicyId = 99999;

        mockMvc.perform(post("/api/v1/member/bookmark/welfare-bookmarks/" + nonExistentPolicyId)
                        .param("policyId", String.valueOf(nonExistentPolicyId))
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                saved.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Policy createTestPolicy(String plcyNo, String plcyNm) {
        return Policy.builder().plcyNo(plcyNo).plcyNm(plcyNm).build();
    }

    private Bookmark createTestBookmark(Member applicant, Policy policy) {
        Bookmark bookmark = new Bookmark();
        try {
            Field policyField = Bookmark.class.getDeclaredField("policy");
            policyField.setAccessible(true);
            policyField.set(bookmark, policy);

            Field applicantField = Bookmark.class.getDeclaredField("applicant");
            applicantField.setAccessible(true);
            applicantField.set(bookmark, applicant);

            LocalDateTime now = LocalDateTime.now();
            Field createdAtField = Bookmark.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(bookmark, now);

            Field modifiedAtField = Bookmark.class.getDeclaredField("modifiedAt");
            modifiedAtField.setAccessible(true);
            modifiedAtField.set(bookmark, now);
        } catch (Exception e) {
            throw new RuntimeException("Bookmark 필드 설정 실패", e);
        }
        return bookmark;
    }
}
