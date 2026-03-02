package com.back.domain.welfare.center.center.repository

import com.back.domain.welfare.center.center.entity.Center
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CenterRepository : JpaRepository<Center, Int> {
    fun deleteByModifiedDateBefore(startOfBatch: LocalDateTime): Int

    @Query("""
        SELECT c FROM Center c 
        WHERE (c.location LIKE %:k1% OR c.address LIKE %:k1%) 
          AND (c.location LIKE %:k2% OR c.address LIKE %:k2%)
    """)
    fun findByKeywords(normalizedK1: String, normalizedK2: String, pageable: Pageable): Page<Center>
}
