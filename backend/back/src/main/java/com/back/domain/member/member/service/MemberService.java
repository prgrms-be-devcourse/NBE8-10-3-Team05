package com.back.domain.member.member.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.auth.store.RedisRefreshTokenStore;
import com.back.domain.auth.util.RefreshTokenGenerator;
import com.back.domain.auth.util.TokenHasher;
import com.back.domain.member.member.dto.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;
import com.back.standard.util.ActorProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ActorProvider actorProvider;
    private final AuthCookieService authCookieService;

    // redis refresh 저장소
    private final RedisRefreshTokenStore redisRefreshTokenStore;

    // refresh 토큰 만료 기간 14일로 가정함
    private static final int REFRESH_DAYS = 14;

    public JoinResponse join(JoinRequest req) {

        // TODO: service단에서의 이중방어인가요?
        //      controller에 @Valid 사용하면 더 좋을 듯 합니다.

        // 요청값 검증
        if (req == null) {
            throw new ServiceException("MEMBER_400", "요청 바디가 비어 있습니다");
        }
        if (req.email() == null || req.email().isBlank()) {
            throw new ServiceException("MEMBER_400", "이메일은 필수 입력값입니다");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new ServiceException("MEMBER_400", "비밀번호는 필수 입력값입니다");
        }
        if (req.name() == null || req.name().isBlank()) {
            throw new ServiceException("MEMBER_400", "이름은 필수 입력값입니다");
        }

        // TODO: email 중복시 "[MEMBER_409] 이미 사용 중인 이메일입니다" 코드까지 사용자에게 보입니다.

        // 이메일 중복 체크
        if (memberRepository.existsByEmailAndStatus(req.email(), Member.MemberStatus.ACTIVE)) {
            throw new ServiceException("MEMBER_409", "이미 사용 중인 이메일입니다");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(req.password());

        // 회원 생성 (엔티티 팩토리 메서드 사용)
        Member member =
                Member.createEmailUser(req.name(), req.email(), encodedPassword, req.rrnFront(), req.rrnBackFirst());

        Member savedMember = memberRepository.save(member);

        return JoinResponse.from(savedMember);
    }

    @Transactional
    public void completeSocialSignup(CompleteSocialSignupRequest req) {

        // TODO: controller 단에서 @Valid 사용하면 더 좋을 듯 합니다.
        if (req == null) throw new ServiceException("MEMBER-400", "요청 바디가 비었습니다.");
        if (req.rrnFront() == null) throw new ServiceException("MEMBER-400", "rrnFront는 필수입니다.");
        if (req.rrnBackFirst() == null) throw new ServiceException("MEMBER-400", "rrnBackFirst는 필수입니다.");

        // 가지고있는 JWT로 Filter에서 member를 받아서 쓴다.
        Member member = actorProvider.getActor();

        // 소셜 미완성 유저만 완료 처리
        if (member.getStatus() != Member.MemberStatus.PRE_REGISTERED) {
            throw new ServiceException("MEMBER-400", "이미 가입 완료된 회원입니다.");
        }

        member.completeSocialSignup(req.rrnFront(), req.rrnBackFirst());
    }

    @Transactional
    public LoginResponse login(LoginRequest req, HttpServletResponse response) {
        // TODO: controller 단에서 @Valid 사용하면 더 좋을 듯 합니다.
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ServiceException("AUTH-400", "email은 필수입니다.");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ServiceException("AUTH-400", "password는 필수입니다.");
        }

        Member member = memberRepository
                .findByEmail(req.getEmail())
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 이메일입니다."));

        if (member.getType() != null && member.getType() != Member.LoginType.EMAIL) {
            throw new ServiceException("AUTH-400", "소셜 로그인 계정입니다. 소셜 로그인을 이용해주세요.");
        }

        if (!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new ServiceException("AUTH-401", "비밀번호가 일치하지 않습니다.");
        }

        // 공통 발급 로직 호출 (access + refresh 쿠키 세팅 + DB 저장)
        String accessToken = issueLoginCookies(member, response);

        return new LoginResponse(member.getId(), member.getName(), accessToken);
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        Member member = actorProvider.getActor();
        return new MeResponse(member.getId(), member.getName(), member.getEmail());
    }

    public record LogoutCookieHeaders(String deleteAccessCookieHeader, String deleteRefreshCookieHeader) {}

    @Transactional
    public LogoutCookieHeaders logout(HttpServletRequest request) {

        // 1) refreshToken 쿠키 원문 읽기
        String rawRefreshToken = getCookieValue(request, "refreshToken");

        // 2) refreshToken이 있으면 Redis에서 삭제(폐기)
        // - DB의 revokedAt = now 로직 대신
        // - Redis에서는 delete가 "폐기" 역할
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String hash = TokenHasher.sha256Hex(rawRefreshToken);
            redisRefreshTokenStore.delete(hash);
        }

        // 3) access/refresh 쿠키 둘 다 삭제 헤더 생성해서 반환
        String deleteAccessCookie = authCookieService.deleteCookie("accessToken");
        String deleteRefreshCookie = authCookieService.deleteCookie("refreshToken");

        return new LogoutCookieHeaders(deleteAccessCookie, deleteRefreshCookie);
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
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // TODO: 이걸 쓰는 곳이 한곳인듯 한데 거기서 memberRepository 부르고 여기는 삭제하는게 더 좋지 않을까요
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

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
    public String issueLoginCookies(Member member, HttpServletResponse response) {

        // =========================
        // 1) AccessToken 발급
        // =========================
        // TODO: 안쓰는 email은 지우는게 좋을 것 같습니다.
        String accessToken = jwtProvider.issueAccessToken(
                member.getId(), member.getEmail() == null ? "" : member.getEmail(), String.valueOf(member.getRole()));

        // =========================
        // 2) RefreshToken 생성 (원문 + 해시)
        // =========================
        // rawRefreshToken = 쿠키에 심는 "원문"
        // ※ DB/Redis 어디에도 원문을 저장하지 않는게 보안상 안전함

        // TODO: 그냥 UUID쓰셔도될 듯 합니다.
        String rawRefreshToken = RefreshTokenGenerator.generate();

        // tokenHash = 서버 저장소(DB/Redis)에 저장하는 "식별자"
        // ※ 유출돼도 원문 복원이 어렵게 SHA-256 해시 사용
        String refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken);

        // =========================
        // 3) Redis에 refresh 저장 + TTL(만료시간)
        // =========================
        // DB의 expiresAt 컬럼을 Redis TTL로 대체함
        // TTL이 끝나면 Redis가 자동으로 key를 삭제 -> "만료 처리" 끝
        redisRefreshTokenStore.save(
                refreshTokenHash, member.getId(), Duration.ofDays(REFRESH_DAYS) // 14일
                );

        // =========================
        // 4) AccessToken 쿠키 생성 + Set-Cookie 헤더로 내려주기
        // =========================
        response.addHeader("Set-Cookie", authCookieService.accessCookie(accessToken, Duration.ofMinutes(20)));

        // =========================
        // 5) RefreshToken 쿠키 생성 + Set-Cookie 헤더로 내려주기
        // =========================
        // refreshToken 쿠키에는 "원문"이 들어감 (클라이언트가 들고 있다가 재발급 요청에 보냄)
        response.addHeader(
                "Set-Cookie", authCookieService.refreshCookie(rawRefreshToken, Duration.ofDays(REFRESH_DAYS)));

        return accessToken;
    }

    @Transactional
    public void issueLoginCookiesWithoutMemberEntity(
            Long memberId, Member.Role memberRole, HttpServletResponse response) {

        String accessToken = jwtProvider.issueAccessTokenWithoutEmail(memberId, String.valueOf(memberRole));

        String rawRefreshToken = RefreshTokenGenerator.generate();

        String refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken);

        redisRefreshTokenStore.save(
                refreshTokenHash, memberId, Duration.ofDays(REFRESH_DAYS) // 14일
                );

        response.addHeader("Set-Cookie", authCookieService.accessCookie(accessToken, Duration.ofMinutes(20)));

        response.addHeader(
                "Set-Cookie", authCookieService.refreshCookie(rawRefreshToken, Duration.ofDays(REFRESH_DAYS)));
    }

    @Transactional
    public Member getOrCreateKakaoMember(String kakaoId, String nickname, String profileImgUrl) {

        return memberRepository
                .findByTypeAndProviderId(Member.LoginType.KAKAO, kakaoId)
                .map(member -> {
                    // 로그인 때마다 최신 프로필 동기화
                    member.updateSocialProfile(nickname, profileImgUrl);
                    return member;
                })
                .orElseGet(() -> {
                    // 최초 소셜 로그인 = 회원가입 처리
                    // email은 카카오에서 scope에 email을 안 받았으니 null 가능
                    // name은 nickname으로 일단 저장
                    Member member = Member.createSocialUser(
                            nickname != null ? nickname : "카카오사용자",
                            null,
                            Member.LoginType.KAKAO,
                            kakaoId,
                            profileImgUrl);

                    return memberRepository.save(member);
                });
    }

    @Transactional
    public void withdraw(HttpServletResponse response) {
        Member member = actorProvider.getActor();

        // 이미 탈퇴한 회원 방어 (엔티티에서 예외 던져도 되고 여기서 해도 됨)
        if (member.getStatus() == Member.MemberStatus.DELETED) {
            throw new ServiceException("MEMBER-400", "이미 탈퇴한 회원입니다.");
        }

        // 1) soft delete
        member.withdraw(); // Status -> delete 로 바꾸는거임

        // 2) refresh 토큰 폐기 (Redis)
        // - access는 짧으니 쿠키 만료로 충분
        // - refresh는 반드시 서버 저장소에서 폐기
        redisRefreshTokenStore.deleteAllByMemberId(member.getId());

        // 2-1) DB refresh도 같이 쓰고 있으면 같이 삭제
        // refreshTokenRepository.deleteByMember_Id(memberId);

        // access/refresh 쿠키 제거
        response.addHeader("Set-Cookie", authCookieService.deleteCookie("accessToken"));
        response.addHeader("Set-Cookie", authCookieService.deleteCookie("refreshToken"));
    }
}
