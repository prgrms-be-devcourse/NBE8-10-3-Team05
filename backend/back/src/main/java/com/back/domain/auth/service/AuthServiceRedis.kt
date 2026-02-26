package com.back.domain.auth.service

import com.back.domain.auth.store.RedisRefreshTokenStore
import com.back.domain.auth.util.TokenHasher
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.security.jwt.JwtProvider
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
@Transactional
class AuthServiceRedis(
    /**
     * ✅ Redis 기반 리프레시 토큰 저장소
     * - key: rt:{tokenHash}
     * - value: memberId
     * - TTL: 만료시간
     */
    private val redisRefreshTokenStore: RedisRefreshTokenStore,

    /**
     * ✅ JwtProvider는 accessToken 발급 시 email/role 클레임이 필요함
     * 그래서 Redis에서 memberId를 찾은 다음, Member를 DB에서 1번 조회한다.
     */
    private val memberRepository: MemberRepository,

    private val jwtProvider: JwtProvider,
    private val memberService: MemberService
) {

    // access token 쿠키 만료 시간 (기존 코드 유지)
    companion object {
        private val ACCESS_MAX_AGE: Duration = Duration.ofMinutes(20)
        private const val REFRESH_DAYS = 14
    }

    /**
     * refreshToken 쿠키를 검증해서 새 accessToken 쿠키(헤더 문자열)를 반환한다.
     *
     * [기존(DB)]
     * - refreshToken hash로 refresh_token 테이블 조회
     * - revoked/expired 확인
     * - member 꺼내서 accessToken 발급
     *
     * [변경(Redis)]
     * - refreshToken hash로 Redis에서 memberId 조회
     * - 없으면(키 없음) => 만료됐거나 폐기된 토큰
     * - memberId로 Member DB 조회 후 accessToken 발급
     */
    @Transactional(readOnly = true)
    fun reissueAccessTokenCookie(request: HttpServletRequest, response: HttpServletResponse) {

        // 1) 요청 쿠키에서 refreshToken 꺼내기 (기존 코드 그대로)
        val rawRefreshToken = getCookieValue(request, "refreshToken")
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw ServiceException("AUTH-401", "refreshToken 쿠키가 없습니다.")
        }

        // 2) refreshToken 원문을 SHA-256 해시로 변환 (기존 그대로)
        val tokenHash = TokenHasher.sha256Hex(rawRefreshToken)

        // 3) Redis에서 memberId 조회
        // - Redis에 key가 없으면: (1) 만료(TTL 끝) (2) 로그아웃/회전으로 delete된 토큰
        val memberId = redisRefreshTokenStore
            .findMemberId(tokenHash)
            .orElseThrow { ServiceException("AUTH-401", "유효하지 않은 refresh token 입니다.") }

        // TODO: id를 가져오면서 role,email도 가져오게 하면 따로 db조회가 필요없을 것 같습니다.
        //      email은 아예 빼도 되셔도 될 것 같습니다.

        // 4) accessToken 발급에 email/role이 필요하므로 Member 조회
        val member = memberRepository
            .findById(memberId)
            .orElseThrow { ServiceException("MEMBER-404", "존재하지 않는 회원입니다.") }

        // refreshToken 자체의 재발급 로직도 필요합니다.
        memberService.issueLoginCookies(member, response)
    }

    /**
     * 요청에서 쿠키 값을 꺼내는 유틸
     * - name에 해당하는 쿠키가 없으면 null 반환
     */
    private fun getCookieValue(request: HttpServletRequest, name: String): String? {
        val cookies = request.cookies ?: return null

        for (c in cookies) {
            if (name == c.name) {
                return c.value
            }
        }
        return null
    }
}
