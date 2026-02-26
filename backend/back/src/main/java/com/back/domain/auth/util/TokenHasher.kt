package com.back.domain.auth.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * 문자열을 SHA-256으로 해시하는 유틸
 *
 * DB에는 refresh token 원문을 저장하지 않고 "hash"만 저장한다.
 * - DB가 털려도 원문을 바로 못 쓰게 하기 위함
 *
 * SHA-256 결과를 hex 문자열로 만들면 길이가 64가 된다.
 */
object TokenHasher {

    /**
     * 입력 문자열을 SHA-256으로 해시한 뒤 hex 문자열로 반환한다.
     *
     * @param raw 원문(예: refresh token UUID)
     * @return SHA-256 hex(길이 64)
     */
    fun sha256Hex(raw: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(raw.toByteArray(StandardCharsets.UTF_8))

            // byte[] -> hex 문자열로 변환
            val sb = StringBuilder()
            for (b in digest) {
                sb.append(String.format("%02x", b))
            }
            return sb.toString()

        } catch (e: Exception) {
            // SHA-256은 자바 표준 알고리즘이라 보통 예외 안 나지만,
            // 혹시 환경 문제 생기면 런타임 예외로 던져서 바로 터지게 함.
            throw IllegalStateException("Failed to hash token with SHA-256", e)
        }
    }
}
