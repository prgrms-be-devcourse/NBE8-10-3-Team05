package com.back.domain.member.geo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GeoApiRequestDto(
        @NotBlank String query, // 검색 질의어 (필수)

        String analyze_type, // similar 또는 exact (기본값 similar)

        @Min(1) @Max(45) Integer page, // 결과 페이지 번호 (1~45, 기본값 1)

        @Min(1) @Max(30) Integer size) {
    public GeoApiRequestDto {
        if (analyze_type == null) analyze_type = "similar";
        if (page == null) page = 1;
        if (size == null) size = 10;
    }
}
