package com.back.standard.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SidoNormalizer {
    /**
     * 시/도 명칭 정규화 (충북 -> 충청북 등)
     * Containing 검색을 고려하여 최소 접두사까지만 매핑
     */
    public static String normalizeSido(String sido) {
        if (sido == null || sido.isBlank()) {
            return sido;
        }

        String trimmed = sido.trim();

        return switch (trimmed) {
            case "서울시" -> "서울";
            case "충북" -> "충청북";
            case "충남" -> "충청남";
            // "전북특별자치도" 떄문에 전북은 적용X
            case "전남" -> "전라남";
            case "경북" -> "경상북";
            case "경남" -> "경상남";
            default -> trimmed;
        };
    }
}
