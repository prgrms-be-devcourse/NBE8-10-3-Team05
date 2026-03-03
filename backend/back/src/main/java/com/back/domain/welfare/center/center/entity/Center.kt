package com.back.domain.welfare.center.center.entity

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "center")
class Center(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var location: String? = null,
    var name: String? = null,
    var address: String? = null ,
    var contact: String? = null,
    var operator: String? = null,
    var corpType: String? = null
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDate: LocalDateTime = LocalDateTime.now() // 초기값을 주어 null 방지
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var modifiedDate: LocalDateTime = LocalDateTime.now()
        protected set
}
