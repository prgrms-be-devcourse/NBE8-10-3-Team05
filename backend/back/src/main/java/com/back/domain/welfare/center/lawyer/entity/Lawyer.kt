package com.back.domain.welfare.center.lawyer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Lawyer(
    @Id
    var id: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val corporation: String,

    val districtArea1: String, // 시/도
    val districtArea2: String? = null // 군/구

) {
    // 군/구
    fun generateId() {
        this.id = String.format("%s_%s_%s_%s", this.name, this.corporation, this.districtArea1, this.districtArea2)
    }
}
