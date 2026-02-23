package com.back.domain.member.member.dto;

// TODO: accessToken 빼는 것이 맞을 듯합니다?
public record LoginResponse(long memberId, String name, String accessToken) {}
