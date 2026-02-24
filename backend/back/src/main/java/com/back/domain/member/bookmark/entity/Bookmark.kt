package com.back.domain.member.bookmark.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.welfare.policy.entity.Policy
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "bookmark")
class Bookmark(

    @ManyToOne
    @JoinColumn(name = "policy_id")
    var policy: Policy? = null,

    @ManyToOne
    @JoinColumn(name = "member_id")
    var applicant: Member? = null

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    private var createdAt: LocalDateTime? = null
    private var modifiedAt: LocalDateTime? = null

    @PrePersist
    protected fun onCreate() {
        createdAt = LocalDateTime.now()
        modifiedAt = LocalDateTime.now()
    }

    @PreUpdate
    protected fun onUpdate() {
        modifiedAt = LocalDateTime.now()
    }
}
