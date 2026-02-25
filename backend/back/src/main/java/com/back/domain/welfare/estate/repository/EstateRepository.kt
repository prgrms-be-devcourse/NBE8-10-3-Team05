package com.back.domain.welfare.estate.repository

import com.back.domain.welfare.estate.entity.Estate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EstateRepository : JpaRepository<Estate, Int> {
    @Deprecated("searchByKeywords 사용 권장")
    fun findByBrtcNmContaining(sido: String?): List<Estate>

    @Deprecated("searchByKeywords 사용 권장")
    fun findByBrtcNmContainingAndSignguNmContaining(sido: String?, signguNm: String?): List<Estate>

    @Query("SELECT DISTINCT e.brtcNm FROM Estate e")
    fun findDistinctBrtcNmBy(): List<String>

    @Query("SELECT DISTINCT e.brtcNm, e.signguNm FROM Estate e")
    fun findDistinctBrtcNmAndSignguNmBy(): List<Array<Any>>

    // TODO: 기능구현을 위해 임시로 query작성한 것 추후 리팩토링 필요
    @Query("""
        SELECT e FROM Estate e 
        WHERE (e.brtcNm LIKE %:k1% OR e.signguNm LIKE %:k1% OR e.fullAdres LIKE %:k1%) 
          AND (e.brtcNm LIKE %:k2% OR e.signguNm LIKE %:k2% OR e.fullAdres LIKE %:k2%)
    """)
    fun searchByKeywords(k1: String?, k2: String?): List<Estate>
}
