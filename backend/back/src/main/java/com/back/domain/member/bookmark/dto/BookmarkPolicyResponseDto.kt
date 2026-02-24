package com.back.domain.member.bookmark.dto

import com.back.domain.welfare.policy.entity.Policy

class BookmarkPolicyResponseDto(val code: Int, val message: String?, val policies: MutableList<Policy?>?)
