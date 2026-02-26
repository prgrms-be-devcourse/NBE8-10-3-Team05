package com.back.domain.welfare.center.center.repository

import com.back.domain.welfare.center.center.entity.Center
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CenterRepository : JpaRepository<Center, Int> {
    fun findByLocation(sido: String?): List<Center>

    fun findByAddressContaining(sido: String?): List<Center>
}
