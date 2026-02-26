package com.back.domain.welfare.center.lawyer.repository

import com.back.domain.welfare.center.lawyer.entity.Lawyer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface LawyerRepository : JpaRepository<Lawyer, String> {
    fun existsByNameAndCorporation(name: String, corporation: String?): Boolean

    fun findByNameIn(names: List<String>): List<Lawyer>

    // 이름, 법인으로 중복체크
    fun findByDistrictArea1AndDistrictArea2Containing(
        area1: String,
        area2: String?,
        pageable: Pageable
    ): Page<Lawyer>
}

