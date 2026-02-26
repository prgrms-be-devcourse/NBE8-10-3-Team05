package com.back.domain.member.member.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "member",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_provider_providerId",
            columnNames = ["type", "provider_id"]
        )
    ]
)
class Member private constructor(

    @Column(nullable = false, length = 20)
    var name: String,

    // 소셜은 email이 없을 수도 있어서 nullable 권장
    @Column(nullable = true)
    var email: String?,

    // 소셜은 password 없음
    @Column(nullable = true, length = 255)
    var password: String?,

    // 소셜은 rrn 없음
    @Column(nullable = true, length = 6)
    var rrnFront: String?,

    @Column(nullable = true, length = 1)
    var rrnBackFirst: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: LoginType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role,

    // 소셜 계정 식별자 (ex. 카카오 user id)
    // EMAIL 회원은 null
    @Column(name = "provider_id", nullable = true) // 단독 unique 제거 → (type, providerId)로 유니크 보장
    var providerId: String?,

    // 프로필 이미지 URL 컬럼 추가
    @Column(name = "profile_img_url", nullable = true)
    var profileImgUrl: String?,

    // 회원상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MemberStatus

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

    var createdAt: LocalDateTime? = null
        private set

    var modifiedAt: LocalDateTime? = null
        private set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        private set

    enum class LoginType {
        EMAIL,
        NAVER,
        KAKAO
    }

    enum class Role {
        ADMIN,
        USER
    }

    enum class MemberStatus {
        PRE_REGISTERED, // 소셜 로그인만 완료(추가정보 미입력)
        ACTIVE, // 필수 정보 입력 완료
        DELETED // 회원 탈퇴
    }

    // 외부에서 new 못 하게 막고, 생성 규칙을 한 곳에 모으는 생성자
    // (Kotlin에서는 private constructor로 막고, companion object 팩토리 메서드로만 생성)
    init {
        // createdAt/modifiedAt을 현재 시각으로 초기화
        val now = LocalDateTime.now()
        this.createdAt = now
        this.modifiedAt = now
    }

    companion object {

        // 이메일 회원가입 전용 생성 함수
        // - type은 EMAIL
        // - role은 USER
        // - createdAt/modifiedAt 자동 세팅
        @JvmStatic
        fun createEmailUser(
            name: String,
            email: String,
            encodedPassword: String,
            rrnFront: String,
            rrnBackFirst: String
        ): Member {
            return Member(
                name = name,
                email = email,
                password = encodedPassword,
                rrnFront = rrnFront,
                rrnBackFirst = rrnBackFirst,
                type = LoginType.EMAIL,
                role = Role.USER,
                providerId = null,
                profileImgUrl = null,
                status = MemberStatus.ACTIVE
            )
        }

        // 소셜 회원 생성 (password/rrn 없음)
        @JvmStatic
        fun createSocialUser(
            name: String,
            email: String?,
            type: LoginType,
            providerId: String,
            profileImgUrl: String?
        ): Member {
            return Member(
                name = name,
                email = email, // 없으면 null 가능
                password = null, // password 없음
                rrnFront = null,
                rrnBackFirst = null, // rrn 없음
                type = type,
                role = Role.USER,
                providerId = providerId,
                profileImgUrl = profileImgUrl,
                status = MemberStatus.PRE_REGISTERED
            )
        }
    }

    // 소셜 로그인 시 프로필 동기화
    fun updateSocialProfile(nickname: String?, profileImgUrl: String?) {
        var changed = false

        if (nickname != null && nickname.isNotBlank() && nickname != this.name) {
            this.name = nickname
            changed = true
        }

        if (profileImgUrl != null &&
            profileImgUrl.isNotBlank() &&
            (this.profileImgUrl == null || profileImgUrl != this.profileImgUrl)
        ) {
            this.profileImgUrl = profileImgUrl
            changed = true
        }

        if (changed) {
            touchModifiedAt()
        }
    }

    // 추가정보 입력 완료 처리 (소셜 PRE → ACTIVE)
    fun completeSocialSignup(rrnFront: String, rrnBackFirst: String) {
        // 필요한 값 검증은 서비스에서 해도 되고 여기서 해도 됨
        this.rrnFront = rrnFront
        this.rrnBackFirst = rrnBackFirst

        this.status = MemberStatus.ACTIVE
        touchModifiedAt()
    }

    // 나중에 정보 수정할 때 modifiedAt 갱신용
    fun touchModifiedAt() {
        this.modifiedAt = LocalDateTime.now()
    }

    fun updateInfo(name: String, email: String?, rrnFront: String?, rrnBackFirst: String?) {
        this.name = name
        this.email = email
        this.rrnFront = rrnFront
        this.rrnBackFirst = rrnBackFirst
        this.touchModifiedAt()
    }

    // 탈퇴 메서드
    fun isDeleted(): Boolean {
        return this.status == MemberStatus.DELETED
    }

    // 탈퇴 메서드
    fun withdraw() {
        if (this.status == MemberStatus.DELETED) {
            throw IllegalStateException("이미 탈퇴한 회원입니다.")
        }

        this.status = MemberStatus.DELETED
        this.deletedAt = LocalDateTime.now()
        touchModifiedAt()
    }
}
