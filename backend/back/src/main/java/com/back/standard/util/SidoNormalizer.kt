package com.back.standard.util

object SidoNormalizer {
    /**
     * 시/도 명칭 정규화 (충북 -> 충청북 등)
     * Containing 검색을 고려하여 최소 접두사까지만 매핑
     */
    @JvmStatic
    fun normalizeSido(sido: String?): String? {
        val trimmed = sido?.trim()?.takeIf { it.isNotBlank() } ?: return sido

        return when (trimmed) {
            "서울시" -> "서울"
            "충북" -> "충청북"
            "충남" -> "충청남"
            "전남" -> "전라남"
            "경북" -> "경상북"
            "경남" -> "경상남"
            else -> trimmed
        }
    }
}


