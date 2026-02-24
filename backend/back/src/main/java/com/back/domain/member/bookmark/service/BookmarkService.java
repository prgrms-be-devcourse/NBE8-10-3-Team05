package com.back.domain.member.bookmark.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.bookmark.entity.Bookmark;
import com.back.domain.member.bookmark.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.welfare.policy.entity.Policy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public List<Policy> getPolicies(Member member) {
        List<Bookmark> bookmarks = bookmarkRepository.getBookmarksByApplicantId(member.getId());

        List<Policy> policies = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            policies.add(bookmark.getPolicy());
        }

        return policies;
    }

    @Transactional
    public String changeBookmarkStatus(Member member, Policy policy) {
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByApplicantAndPolicy(member, policy);

        if (existingBookmark.isPresent()) {
            // 북마크가 존재하면 삭제
            bookmarkRepository.delete(existingBookmark.get());
            return "북마크가 해제되었습니다.";
        } else {
            // 북마크가 없으면 새로 생성
            Bookmark bookmark = new Bookmark(policy, member); // 생성자로 주입
            bookmarkRepository.save(bookmark);
            return "북마크가 추가되었습니다.";
        }
    }
}
