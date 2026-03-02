package com.back.standard.util

object SidoNormalizer {
    /**
     * 시/도 명칭 정규화 (충북 -> 충청북 등)
     * Containing 검색을 고려하여 최소 접두사까지만 매핑
     */
    fun normalizeSido(sido: String): String {
        val trimmed = sido.trim().takeIf { it.isNotBlank() } ?: return sido

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

    fun normalizeSido2(raw: String?): String {
        val s = raw?.trim()?.replace(" ", "") ?: return ""
        if (s.length < 2) return s

        // "경상남도" -> "경남", "충청북도" -> "충북" (1번째, 3번째 글자 조합)
        return if (s.contains(Regex("^(경상|충청|전라)"))) {
            "${s[0]}${s[2]}"
        } else {
            s.substring(0, 2) // "서울", "경기", "강원" 등
        }
    }
}


