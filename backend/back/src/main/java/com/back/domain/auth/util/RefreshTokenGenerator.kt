package com.back.domain.auth.util

import java.util.UUID

object RefreshTokenGenerator {

    // 리프레시 토큰 uuid 생성
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
