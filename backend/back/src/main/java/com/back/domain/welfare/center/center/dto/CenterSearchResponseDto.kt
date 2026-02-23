package com.back.domain.welfare.center.center.dto;

import java.util.List;

import com.back.domain.welfare.center.center.entity.Center;

public record CenterSearchResponseDto(List<Center> centerList) {}
