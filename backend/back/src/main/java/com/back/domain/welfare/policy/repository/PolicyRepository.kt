package com.back.domain.welfare.policy.repository

import com.back.domain.welfare.policy.entity.Policy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PolicyRepository : JpaRepository<Policy, Int> {

    fun findPolicyById(policyId: Int): Policy?

    @Query("select p.plcyNo from Policy p where p.plcyNo in :plcyNos")
    fun findExistingPlcyNos(plcyNos: Set<String>): Set<String>
}