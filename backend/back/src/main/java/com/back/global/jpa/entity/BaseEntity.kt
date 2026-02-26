package com.back.global.jpa.entity

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @CreatedDate
    @Column(updatable = false)
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    val modifiedDate: LocalDateTime? = null

) {

    /**
     * JPA 엔티티 동등성 비교
     * - Hibernate proxy 대응
     * - 영속성 ID 기준 비교
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as BaseEntity

        return id != 0L && id == other.id
    }

    override fun hashCode(): Int =
        Hibernate.getClass(this).hashCode()
}