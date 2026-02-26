package com.back.global.security

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Component
class SecurityBeanConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
