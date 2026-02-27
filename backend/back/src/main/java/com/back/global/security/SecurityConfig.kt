package com.back.global.security

import com.back.global.security.jwt.JwtAuthenticationFilter
import com.back.global.security.jwt.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@Profile("!test")
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customOAuth2LoginSuccessHandler: CustomOAuth2LoginSuccessHandler,
    private val customOAuth2UserService: CustomOAuth2UserService
) {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { csrf -> csrf.disable() }
            .headers { headers ->
                headers.frameOptions { frameOptions ->
                    frameOptions.sameOrigin()
                }
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/favicon.ico", "/h2-console/**", "/error")
                    .permitAll()
                    // 기본적으로 모든 조회는 가능하지만 user인증이 필요한 기능만 block
                    .requestMatchers("/api/v1/welfare/**", "/actuator/**")
                    // "/actuator/**" 허용해주므로서 모니터링 가능하도록 함
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/member/member/login",
                        "/api/v1/member/member/logout",
                        "/api/v1" + "/member/member/join"
                    )
                    .permitAll()
                    .requestMatchers("/api/v1/auth/reissue", "/batchTest")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }

            // OAuth2 로그인은 인가 요청(state)을 여러 요청에 걸쳐 검증해야 하므로
            // 기본 구현은 HttpSession을 사용함.
            // STATELESS로 설정하면 OAuth2 인증 과정이 깨지므로,
            // 로그인 과정에서만 세션을 허용하는 IF_REQUIRED로 설정.
            // .sessionManagement(sm -> sm.sessionCreationPolicy(IF_REQUIRED))
            .sessionManagement { sessionManagement ->
                sessionManagement.sessionCreationPolicy(STATELESS)
            }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }.successHandler(customOAuth2LoginSuccessHandler)
            }
            // TODO: customOAuth2LoginSuccessHandler 에서 socialLogin한 유저에게 accessToken과 refreshToken발급 x

            // 토큰 없거나 인증 실패 → 401로 통일
            .exceptionHandling { eh ->
                eh.authenticationEntryPoint { _, resp, _ ->
                    resp.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized")
                }
            }
            // JWT 필터를 Spring Security 필터 체인에 등록
            // UsernamePasswordAuthenticationFilter 이전에 실행되게 두는 게 일반적
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
