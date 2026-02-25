package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.entity.Policy.Companion.builder
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
class PolicyFetchService(
    private val policyRepository: PolicyRepository,
    private val policyApiClient: PolicyApiClient,
    private val objectMapper: ObjectMapper,
    private val policyElasticSearchService: PolicyElasticSearchService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    @Deprecated("기존 배치 방식")
    fun fetchAndSavePolicies(requestDto: PolicyFetchRequestDto) {

        val pageSize = 100
        var pageNum = 1

        val firstResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize)

        val totalCnt = firstResponse.result?.pagging?.totCount ?: return
        val totalPages = ceil(totalCnt.toDouble() / pageSize).toInt()

        savePolicies(firstResponse.result?.youthPolicyList)

        for (page in 2..totalPages) {
            Thread.sleep(500) // API 과부하 방지

            val nextResponse = policyApiClient.fetchPolicyPage(requestDto, page, pageSize)
            savePolicies(nextResponse.result?.youthPolicyList)
        }

        policyElasticSearchService.reindexAllFromDb()
    }

    private fun savePolicies(items: List<PolicyItem?>?) {

        val safeItems = items
            ?.filterNotNull()
            ?.filter { it.plcyNo != null }
            ?.takeIf { it.isNotEmpty() }
            ?: return

        // 페이지 내 plcyNo 수집
        val pagePlcyNos = safeItems.mapNotNull { it.plcyNo }.toSet()

        // DB 중복 조회
        val existingPlcyNos = policyRepository.findExistingPlcyNos(pagePlcyNos)

        val newPolicies = safeItems
            .asSequence()
            .filter { it.plcyNo !in existingPlcyNos }
            .map { toEntity(it) }
            .toList()

        if (newPolicies.isEmpty()) {
            log.info("저장할 신규 정책 없음 (페이지 스킵)")
            return
        }

        policyRepository.saveAll(newPolicies)
    }

    private fun toEntity(item: PolicyItem): Policy =
        try {
            builder()
                .plcyNo(item.plcyNo)
                .plcyNm(item.plcyNm)
                .plcyKywdNm(item.plcyKywdNm)
                .plcyExplnCn(item.plcyExplnCn)
                .plcySprtCn(item.plcySprtCn)
                .sprvsnInstCdNm(item.sprvsnInstCdNm)
                .operInstCdNm(item.operInstCdNm)
                .aplyPrdSeCd(item.aplyPrdSeCd)
                .bizPrdBgngYmd(item.bizPrdBgngYmd)
                .bizPrdEndYmd(item.bizPrdEndYmd)
                .plcyAplyMthdCn(item.plcyAplyMthdCn)
                .aplyUrlAddr(item.aplyUrlAddr)
                .sbmsnDcmntCn(item.sbmsnDcmntCn)
                .sprtTrgtMinAge(item.sprtTrgtMinAge)
                .sprtTrgtMaxAge(item.sprtTrgtMaxAge)
                .sprtTrgtAgeLmtYn(item.sprtTrgtAgeLmtYn)
                .mrgSttsCd(item.mrgSttsCd)
                .earnCndSeCd(item.earnCndSeCd)
                .earnMinAmt(item.earnMinAmt)
                .earnMaxAmt(item.earnMaxAmt)
                .zipCd(item.zipCd)
                .jobCd(item.jobCd)
                .schoolCd(item.schoolCd)
                .aplyYmd(item.aplyYmd)
                .sBizCd(item.sbizCd)
                .rawJson(objectMapper.writeValueAsString(item))
                .build()
        } catch (e: Exception) {
            throw RuntimeException("Entity 변환 실패", e)
        }
}
