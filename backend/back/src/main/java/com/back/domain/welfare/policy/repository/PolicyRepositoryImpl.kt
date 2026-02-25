package com.back.domain.welfare.policy.repository

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto
import com.back.domain.welfare.policy.dto.QPolicySearchResponseDto
import com.back.domain.welfare.policy.entity.QPolicy.policy
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PolicyRepositoryImpl(
    private val queryFactory: JPAQueryFactory //가끔 Could not autowire 뜨는데 무시해도 된다.IDE의 오탐(false positive)
) : PolicyRepositoryCustom {

    override fun search(condition: PolicySearchRequestDto): List<PolicySearchResponseDto> {

        val builder = BooleanBuilder().apply {

            condition.sprtTrgtMinAge?.let {
                and(Expressions.numberTemplate(Integer::class.java, "CAST({0} AS INTEGER)", policy.sprtTrgtMinAge).goe(it))
            }

            condition.sprtTrgtMaxAge?.let {
                and(Expressions.numberTemplate(Integer::class.java, "CAST({0} AS INTEGER)", policy.sprtTrgtMaxAge).loe(it))
            }

            condition.zipCd?.let {
                and(policy.zipCd.eq(it))
            }

            condition.schoolCd?.let {
                and(policy.schoolCd.eq(it))
            }

            condition.jobCd?.let {
                and(policy.jobCd.eq(it))
            }

            condition.earnMinAmt?.let {
                and(Expressions.numberTemplate(Integer::class.java, "CAST({0} AS INTEGER)", policy.earnMinAmt).goe(it))
            }

            condition.earnMaxAmt?.let {
                and(Expressions.numberTemplate(Integer::class.java, "CAST({0} AS INTEGER)", policy.earnMaxAmt).loe(it))
            }
        }

        return queryFactory
            .select(
                QPolicySearchResponseDto(
                    policy.id,
                    policy.plcyNo,
                    policy.plcyNm,
                    policy.plcyExplnCn,
                    policy.plcySprtCn,
                    policy.plcyKywdNm,
                    policy.sprtTrgtMinAge,
                    policy.sprtTrgtMaxAge,
                    policy.zipCd,
                    policy.schoolCd,
                    policy.jobCd,
                    policy.earnMinAmt,
                    policy.earnMaxAmt,
                    policy.aplyYmd,
                    policy.aplyUrlAddr,
                    policy.plcyAplyMthdCn,
                    policy.sbmsnDcmntCn,
                    policy.operInstCdNm
                )
            )
            .from(policy)
            .where(builder)
            .fetch()
    }
}