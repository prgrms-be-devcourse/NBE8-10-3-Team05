package com.back.domain.member.member.service

import com.back.domain.auth.store.RedisRefreshTokenStore
import com.back.domain.auth.util.RefreshTokenGenerator
import com.back.domain.auth.util.TokenHasher
import com.back.domain.member.member.dto.*
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import com.back.global.security.jwt.JwtProvider
import com.back.standard.util.ActorProvider
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.Optional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val actorProvider: ActorProvider,
    private val authCookieService: AuthCookieService,

    // redis refresh 저장소
    private val redisRefreshTokenStore: RedisRefreshTokenStore,
) {

    companion object {
        // refresh 토큰 만료 기간 14일로 가정함
        private const val REFRESH_DAYS = 14
    }

    fun join(req: JoinRequest): JoinResponse {

        // TODO: email 중복시 "[MEMBER_409] 이미 사용 중인 이메일입니다" 코드까지 사용자에게 보입니다.

        // 이메일 중복 체크
        if (memberRepository.existsByEmailAndStatus(req.email, Member.MemberStatus.ACTIVE)) {
            throw ServiceException("MEMBER_409", "이미 사용 중인 이메일입니다")
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(req.password)!!

        // 회원 생성 (엔티티 팩토리 메서드 사용)
        val member = Member.createEmailUser(
            req.name,
            req.email,
            encodedPassword,
            req.rrnFront,
            req.rrnBackFirst
        )

        val savedMember = memberRepository.save(member)

        return JoinResponse.from(savedMember)
    }

    @Transactional
    fun completeSocialSignup(req: CompleteSocialSignupRequest) {
        // 가지고있는 JWT로 Filter에서 member를 받아서 쓴다.
        val member = actorProvider.getActor()

        // 소셜 미완성 유저만 완료 처리
        if (member.status != Member.MemberStatus.PRE_REGISTERED) {
            throw ServiceException("MEMBER-400", "이미 가입 완료된 회원입니다.")
        }

        member.completeSocialSignup(req.rrnFront, req.rrnBackFirst)
    }

    @Transactional
    fun login(req: LoginRequest, response: HttpServletResponse): LoginResponse {

        val member = memberRepository
            .findByEmail(req.email)
            .orElseThrow { ServiceException("MEMBER-404", "존재하지 않는 이메일입니다.") }

        if (member.type != Member.LoginType.EMAIL) {
            throw ServiceException("AUTH-400", "소셜 로그인 계정입니다. 소셜 로그인을 이용해주세요.")
        }

        if (!passwordEncoder.matches(req.password, member.password)) {
            throw ServiceException("AUTH-401", "비밀번호가 일치하지 않습니다.")
        }

        // 공통 발급 로직 호출 (access + refresh 쿠키 세팅 + DB 저장)
        val accessToken = issueLoginCookies(member, response)

        return LoginResponse(requireNotNull(member.id), member.name, accessToken)
    }

    @Transactional(readOnly = true)
    fun me(): MeResponse {
        val member = actorProvider.getActor()
        return MeResponse(requireNotNull(member.id), member.name, member.email)
    }

    @Transactional
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {

        // 1) refreshToken 쿠키 원문 읽기
        val rawRefreshToken = getCookieValue(request, "refreshToken")

        // 2) refreshToken이 있으면 Redis에서 삭제(폐기)
        // - DB의 revokedAt = now 로직 대신
        // - Redis에서는 delete가 "폐기" 역할
        if (rawRefreshToken != null && rawRefreshToken.isNotBlank()) {
            val hash = TokenHasher.sha256Hex(rawRefreshToken)
            redisRefreshTokenStore.delete(hash)
        }

        // 3) access/refresh 쿠키 둘 다 삭제 헤더 생성해서 응답에 세팅
        val deleteAccessCookie = authCookieService.deleteCookie("accessToken")
        val deleteRefreshCookie = authCookieService.deleteCookie("refreshToken")

        response.addHeader("Set-Cookie", deleteAccessCookie)
        response.addHeader("Set-Cookie", deleteRefreshCookie)
    }

    //    @Transactional
    //    public LogoutCookieHeaders logout(HttpServletRequest request) {
    //
    //        // 1) refreshToken 쿠키 원문 읽기
    //        String rawRefreshToken = getCookieValue(request, "refreshToken");
    //
    //        // 2) refreshToken이 있으면 DB에서 찾아서 폐기(revoke)
    //        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
    //            String hash = TokenHasher.sha256Hex(rawRefreshToken);
    //
    //            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
    //                rt.revoke(); // revokedAt = now
    //                // rt가 영속 상태면 save 없어도 되지만, 안전하게 save 해도 됨
    //                refreshTokenRepository.save(rt);
    //            });
    //
    //            // delete로 하고 싶으면 revoke 대신 이걸로 교체 가능
    //            // refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshTokenRepository::delete);
    //        }
    //
    //        // access/refresh 쿠키 둘 다 삭제 헤더 생성해서 반환
    //        String deleteAccessCookie = buildDeleteCookieHeader("accessToken");
    //        String deleteRefreshCookie = buildDeleteCookieHeader("refreshToken");
    //
    //        return new LogoutCookieHeaders(deleteAccessCookie, deleteRefreshCookie);
    //    }

    // 요청에서 쿠키값 꺼내기
    private fun getCookieValue(request: HttpServletRequest, name: String): String? {
        val cookies: Array<Cookie>? = request.cookies
        if (cookies == null) return null

        for (cookie in cookies) {
            if (name == cookie.name) {
                return cookie.value
            }
        }
        return null
    }

    // TODO: 이걸 쓰는 곳이 한곳인듯 한데 거기서 memberRepository 부르고 여기는 삭제하는게 더 좋지 않을까요
//    @Transactional(readOnly = true)
//    fun findById(id: Long): Optional<Member> {
//        return memberRepository.findById(id)
//    }

    //    @Transactional
    //    public String issueLoginCookies(Member member, HttpServletResponse response) {
    //
    //        // 1) AccessToken 발급
    //        String accessToken = jwtProvider.issueAccessToken(
    //                member.getId(), member.getEmail() == null ? "" : member.getEmail(),
    // String.valueOf(member.getRole()));
    //
    //        // 2) RefreshToken 생성
    //        String rawRefreshToken = RefreshTokenGenerator.generate();
    //        String refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken);
    //
    //        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);
    //
    //        RefreshToken refreshToken = RefreshToken.create(member, refreshTokenHash, expiresAt);
    //        refreshTokenRepository.save(refreshToken);
    //
    //        // 3) AccessToken 쿠키
    //        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
    //                .httpOnly(true)
    //                .secure(false) // dev
    //                .path("/")
    //                .sameSite("Lax")
    //                .maxAge(Duration.ofMinutes(20))
    //                .build();
    //
    //        // 4) RefreshToken 쿠키
    //        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", rawRefreshToken)
    //                .httpOnly(true)
    //                .secure(false)
    //                .path("/")
    //                .sameSite("Lax")
    //                .maxAge(Duration.ofDays(14))
    //                .build();
    //
    //        response.addHeader("Set-Cookie", accessCookie.toString());
    //        response.addHeader("Set-Cookie", refreshCookie.toString());
    //        return accessToken;
    //    }

    // TODO: 이 함수는 jwtProvider에 들어가야하지 않을까요?
    @Transactional
    fun issueLoginCookies(member: Member, response: HttpServletResponse): String {

        // =========================
        // 1) AccessToken 발급
        // =========================
        val accessToken = jwtProvider.issueAccessToken(
            requireNotNull(member.id),
            member.role.toString()
        )

        // =========================
        // 2) RefreshToken 생성 (원문 + 해시)
        // =========================
        // rawRefreshToken = 쿠키에 심는 "원문"
        // ※ DB/Redis 어디에도 원문을 저장하지 않는게 보안상 안전함

        // TODO: 그냥 UUID쓰셔도될 듯 합니다.
        val rawRefreshToken = RefreshTokenGenerator.generate()

        // tokenHash = 서버 저장소(DB/Redis)에 저장하는 "식별자"
        // ※ 유출돼도 원문 복원이 어렵게 SHA-256 해시 사용
        val refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken)

        // =========================
        // 3) Redis에 refresh 저장 + TTL(만료시간)
        // =========================
        // DB의 expiresAt 컬럼을 Redis TTL로 대체함
        // TTL이 끝나면 Redis가 자동으로 key를 삭제 -> "만료 처리" 끝
        redisRefreshTokenStore.save(
            refreshTokenHash,
            requireNotNull(member.id),
            Duration.ofDays(REFRESH_DAYS.toLong()) // 14일
        )

        // =========================
        // 4) AccessToken 쿠키 생성 + Set-Cookie 헤더로 내려주기
        // =========================
        response.addHeader("Set-Cookie", authCookieService.accessCookie(accessToken, Duration.ofMinutes(20)))

        // =========================
        // 5) RefreshToken 쿠키 생성 + Set-Cookie 헤더로 내려주기
        // =========================
        // refreshToken 쿠키에는 "원문"이 들어감 (클라이언트가 들고 있다가 재발급 요청에 보냄)
        response.addHeader(
            "Set-Cookie",
            authCookieService.refreshCookie(rawRefreshToken, Duration.ofDays(REFRESH_DAYS.toLong()))
        )

        return accessToken
    }

    @Transactional
    fun issueLoginCookiesWithoutMemberEntity(
        memberId: Long,
        memberRole: Member.Role,
        response: HttpServletResponse
    ) {

        val accessToken = jwtProvider.issueAccessToken(memberId, memberRole.toString())

        val rawRefreshToken = RefreshTokenGenerator.generate()

        val refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken)

        redisRefreshTokenStore.save(
            refreshTokenHash,
            memberId,
            Duration.ofDays(REFRESH_DAYS.toLong()) // 14일
        )

        response.addHeader("Set-Cookie", authCookieService.accessCookie(accessToken, Duration.ofMinutes(20)))

        response.addHeader(
            "Set-Cookie",
            authCookieService.refreshCookie(rawRefreshToken, Duration.ofDays(REFRESH_DAYS.toLong()))
        )
    }

    @Transactional
    fun getOrCreateKakaoMember(kakaoId: String, nickname: String?, profileImgUrl: String?): Member {

        return memberRepository
            .findByTypeAndProviderId(Member.LoginType.KAKAO, kakaoId)
            .map { member ->
                // 로그인 때마다 최신 프로필 동기화
                member.updateSocialProfile(nickname, profileImgUrl)
                member
            }
            .orElseGet {
                // 최초 소셜 로그인 = 회원가입 처리
                // email은 카카오에서 scope에 email을 안 받았으니 null 가능
                // name은 nickname으로 일단 저장
                val member = Member.createSocialUser(
                    if (nickname != null) nickname else "카카오사용자",
                    null,
                    Member.LoginType.KAKAO,
                    kakaoId,
                    profileImgUrl
                )

                memberRepository.save(member)
            }
    }

    @Transactional
    fun withdraw(response: HttpServletResponse) {
        val member = actorProvider.getActor()

        // 이미 탈퇴한 회원 방어 (엔티티에서 예외 던져도 되고 여기서 해도 됨)
        if (member.status == Member.MemberStatus.DELETED) {
            throw ServiceException("MEMBER-400", "이미 탈퇴한 회원입니다.")
        }

        // 1) soft delete
        member.withdraw() // Status -> delete 로 바꾸는거임

        // 2) refresh 토큰 폐기 (Redis)
        // - access는 짧으니 쿠키 만료로 충분
        // - refresh는 반드시 서버 저장소에서 폐기
        redisRefreshTokenStore.deleteAllByMemberId(requireNotNull(member.id))

        // 2-1) DB refresh도 같이 쓰고 있으면 같이 삭제
        // refreshTokenRepository.deleteByMember_Id(memberId);

        // access/refresh 쿠키 제거
        response.addHeader("Set-Cookie", authCookieService.deleteCookie("accessToken"))
        response.addHeader("Set-Cookie", authCookieService.deleteCookie("refreshToken"))
    }
}
