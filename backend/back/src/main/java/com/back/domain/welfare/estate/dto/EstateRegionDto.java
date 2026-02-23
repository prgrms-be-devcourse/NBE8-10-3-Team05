package com.back.domain.welfare.estate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EstateRegionDto {
    private String name;
    private String parentName; // 부모 이름 (null이면 최상위)
    private int level;
}
