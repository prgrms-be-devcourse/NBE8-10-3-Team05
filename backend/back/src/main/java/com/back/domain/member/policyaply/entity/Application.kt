package com.back.domain.member.policyaply.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.welfare.policy.entity.Policy
import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "apply")
@Getter
@Setter
@NoArgsConstructor
class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne
    @JoinColumn(name = "policy_id")
    var policy: Policy? = null

    @ManyToOne
    @JoinColumn(name = "member_id")
    var applicant: Member? = null

    @CreationTimestamp
    private val createdAt: LocalDateTime? = null

    @UpdateTimestamp
    private val modifiedAt: LocalDateTime? = null
}
