package com.back.domain.member.geo.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class GeoApiRequestDto(
    @param:NotBlank val query: @NotBlank String?,
    analyze_type: String?,
    @Min(1) @Max(45) page: Int?,
    @Min(1) @Max(30) size: Int?
) {
    val analyze_type: String?
    val page: @Min(1) @Max(45) Int?
    val size: @Min(1) @Max(30) Int?

    init {
        var analyze_type = analyze_type
        var page = page
        var size = size
        if (analyze_type == null) analyze_type = "similar"
        if (page == null) page = 1
        if (size == null) size = 10
        this.analyze_type = analyze_type
        this.page = page
        this.size = size
    }
}
