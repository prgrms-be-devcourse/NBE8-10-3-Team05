package com.back.domain.member.bookmark.dto;

import java.util.List;

import com.back.domain.welfare.policy.entity.Policy;

public record BookmarkPolicyResponseDto(int code, String message, List<Policy> policies) {}
