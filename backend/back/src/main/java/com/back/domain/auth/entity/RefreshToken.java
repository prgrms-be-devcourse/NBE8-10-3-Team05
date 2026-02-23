package com.back.domain.auth.entity;

import java.time.LocalDateTime;

import com.back.domain.member.member.entity.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 엔티티
 *
 * 역할:
 * - Access Token이 만료되었을 때
 * - 새 Access Token을 발급받기 위해 사용하는 토큰
 *
 * 핵심 포인트:
 * - refresh token "원문"은 DB에 저장하지 않고
 * - 보안을 위해 hash 값만 저장한다
 */
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 회원의 refresh token인지 / 한 회원은 여러 번 로그인할 수 있으므로 여러 refresh token을 가질 수 있다
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // refresh token의 해시 값
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    // 리프레시 토큰 생성 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 리프레시 토큰 만료 시간
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // TODO: redis.delete(key)로 완전히 삭제하시던데
    //      soft delete는 그럼 이제 안쓰시나요?

    /**
     * refresh token 폐기 시각
     *
     * 사용 예:
     * - 로그아웃 시
     * - 보안 이슈로 강제 만료 시
     *
     * 규칙:
     * - null이면 아직 유효
     * - 값이 있으면 이미 폐기된 토큰
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    private RefreshToken(Member member, String tokenHash, LocalDateTime expiresAt) {
        this.member = member;
        this.tokenHash = tokenHash;

        // 생성 시점 기준으로 시간 자동 세팅
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.expiresAt = expiresAt;
        this.revokedAt = null; // 처음 생성될 때는 아직 폐기되지 않음
    }

    public static RefreshToken create(Member member, String tokenHash, LocalDateTime expiresAt) {
        return new RefreshToken(member, tokenHash, expiresAt);
    }

    // 리프레시토큰 폐기처리
    // 로그아웃할 때 호출
    // 폐기시간 (현재 시각)
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    // 이미 폐기된 토큰인지 확인
    public boolean isRevoked() {
        return revokedAt != null;
    }

    // 만료된 토큰인지 확인
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    // 폐기되지 않았고 만료되지 않은 리프레시토큰인지 확인
    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }
}
