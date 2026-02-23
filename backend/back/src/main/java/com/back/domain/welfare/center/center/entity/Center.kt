package com.back.domain.welfare.center.center.entity

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto.CenterDto
import jakarta.persistence.*

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
) { }
