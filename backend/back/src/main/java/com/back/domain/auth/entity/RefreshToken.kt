package com.back.domain.auth.entity

import com.back.domain.member.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

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
class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    // 어떤 회원의 refresh token인지 / 한 회원은 여러 번 로그인할 수 있으므로 여러 refresh token을 가질 수 있다
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    lateinit var member: Member
        protected set

    // refresh token의 해시 값
    @Column(name = "token_hash", nullable = false, length = 64)
    lateinit var tokenHash: String
        protected set

    // 리프레시 토큰 생성 시간
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    // 리프레시 토큰 만료 시간
    @Column(name = "expires_at", nullable = false)
    lateinit var expiresAt: LocalDateTime
        protected set

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
    var revokedAt: LocalDateTime? = null
        protected set

    // JPA 기본 생성자 (kotlin-jpa no-arg 플러그인이 만들어주지만,
    // 명시해두면 IDE/리플렉션 상황에서도 더 명확함)
    protected constructor()

    private constructor(member: Member, tokenHash: String, expiresAt: LocalDateTime) {
        this.member = member
        this.tokenHash = tokenHash

        // 생성 시점 기준으로 시간 자동 세팅
        val now = LocalDateTime.now()
        this.createdAt = now
        this.expiresAt = expiresAt
        this.revokedAt = null // 처음 생성될 때는 아직 폐기되지 않음
    }

    companion object {
        fun create(member: Member, tokenHash: String, expiresAt: LocalDateTime): RefreshToken {
            return RefreshToken(member, tokenHash, expiresAt)
        }
    }

    // 리프레시토큰 폐기처리
    // 로그아웃할 때 호출
    // 폐기시간 (현재 시각)
    fun revoke() {
        this.revokedAt = LocalDateTime.now()
    }

    // 이미 폐기된 토큰인지 확인
    fun isRevoked(): Boolean {
        return revokedAt != null
    }

    // 만료된 토큰인지 확인
    fun isExpired(): Boolean {
        return expiresAt.isBefore(LocalDateTime.now())
    }

    // 폐기되지 않았고 만료되지 않은 리프레시토큰인지 확인
    fun isActive(): Boolean {
        return !isRevoked() && !isExpired()
    }
}
