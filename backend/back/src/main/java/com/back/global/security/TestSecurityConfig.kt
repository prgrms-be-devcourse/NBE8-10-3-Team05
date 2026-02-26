package com.back.global.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Profile("test")
class TestSecurityConfig {

    @Bean
    @Throws(Exception::class)
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .headers { headers ->
                headers.frameOptions { frame ->
                    frame.sameOrigin()
                }
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/h2-console/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll()
            }
        return http.build()
    }

}
