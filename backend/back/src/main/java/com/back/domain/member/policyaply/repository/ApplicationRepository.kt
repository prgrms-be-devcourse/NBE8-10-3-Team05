package com.back.domain.member.policyaply.repository

import com.back.domain.member.policyaply.entity.Application
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationRepository : JpaRepository<Application, Long> {
    fun findAllByApplicant_Id(applicantId: Long?): MutableList<Application?>?

    fun getApplicationById(id: Long?): Application?
}
