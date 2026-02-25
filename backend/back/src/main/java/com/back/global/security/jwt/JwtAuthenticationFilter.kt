package com.back.global.security.jwt

import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Arrays

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val memberRepository: MemberRepository,
    private val env: Environment
) : OncePerRequestFilter() {

    // 토큰 없이도 허용할 경로들 (SecurityConfig의 permitAll과 맞춰주는 게 중요)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/h2-console")
            || path.startsWith("/api/v1/welfare") // 기본적으로 조회는 모두 가능하도록
            || path == "/api/v1/member/member/join"
            || path == "/api/v1/member/member/login"
            || path == "/api/v1/member/member/logout"
            || path == "/api/v1/auth/reissue"
            || path == "/favicon.ico"
            || path == "/batchTest"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 테스트에서는 인증 로직을 아예 건너뛰고 전부 통과
        if (Arrays.asList(*env.activeProfiles).contains("test")) {
            filterChain.doFilter(request, response)
            return
        }

        // 토큰 추출: Authorization 우선, 없으면 쿠키
        val token = resolveToken(request)

        // String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1) Authorization 헤더가 없으면 그냥 다음으로
        // (단, 보호된 경로는 결국 Security에서 401 처리됨)
        if (token.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        //        // 2) Bearer 형식 검사
        //        if (!authHeader.startsWith("Bearer ")) {
        //            throw new ServiceException("AUTH-401", "Authorization 헤더가 Bearer 형식이 아닙니다.");
        //        }
        //
        //        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            // 3) 토큰 검증 + Claims 추출
            val claims: Claims = jwtProvider.getClaims(token)
            //            System.out.println("JWT subject = " + claims.getSubject());
            //            System.out.println("JWT role = " + claims.get("role"));

            // 4) Claims에서 필요한 정보 추출
            val memberId = claims.subject.toLong()
            val role = claims["role"].toString() // 예: USER

            // 탈퇴 회원 차단 (soft delete)
            // TODO: 탈퇴회원 처리는 respotiory를 조회하는 것이 아닌,
            //      Client측의 cookie를 삭제 + redis의 refreshToken제거 가 더 좋을 듯 합니다.
            //      현재 withdraw하실 때 token다 날리니까 이부분은 주석처리 하셔도 될듯 합니다.
            //            Member member = memberRepository
            //                    .findById(memberId)
            //                    .orElseThrow(() -> new ServiceException("AUTH-401", "존재하지 않는 회원입니다."));
            //
            //            if (member.getStatus() == Member.MemberStatus.DELETED) {
            //                throw new ServiceException("AUTH-401", "탈퇴한 회원입니다.");
            //            }

            // 5) Spring Security 인증 객체 생성
            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

            val authentication = UsernamePasswordAuthenticationToken(
                memberId, // principal (간단히 memberId 넣음)
                null,
                authorities
            )

            // 6) SecurityContext에 저장 → 이후 컨트롤러에서 인증된 사용자로 인식됨
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            // 토큰 위조/만료/파싱 실패
            sendUnauthorizedResponse(response, e.message)
        }
    }

    // 프론트의 fetchWithAuth가 읽음. 내용은 아무거나
    private fun sendUnauthorizedResponse(response: HttpServletResponse, message: String?) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED // 401
        response.contentType = "application/json;charset=UTF-8"
        val safeMsg = (message ?: "Unauthorized").replace("\"", "\\\"")
        val json = """{"resultCode": "AUTH-401", "msg": "$safeMsg"}"""
        response.writer.write(json)
    }

    // TODO: JWTProvider에 있어야 더 좋을 것 같습니다.
    // TODO: 토큰 발급이 아니라서 JWTProvider 부분이 아닌것 같습니다
    // Authorization: Bearer 토큰이 있으면 그걸 사용하고, 없으면 HttpOnly 쿠키(accessToken)에서 읽는다.
    private fun resolveToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

        // 헤더가 있으면 Bearer 형식만 허용 (기존 정책 유지)
        if (!authHeader.isNullOrBlank()) {
            if (!authHeader.startsWith("Bearer ")) {
                throw ServiceException("AUTH-401", "Authorization 헤더가 Bearer 형식이 아닙니다.")
            }
            return authHeader.substring("Bearer ".length).trim()
        }

        // 헤더가 없으면 쿠키에서 accessToken 읽기
        return getCookieValue(request, "accessToken")
    }

    private fun getCookieValue(request: HttpServletRequest, name: String): String? {
        val cookies: Array<Cookie> = request.cookies ?: return null

        for (cookie in cookies) {
            if (name == cookie.name) {
                val value = cookie.value
                return if (value.isNullOrBlank()) null else value
            }
        }
        return null
    }
}
