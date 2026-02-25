package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import kotlin.math.ceil

@Service
class PolicyFetchService(
    private val policyRepository: PolicyRepository,
    private val policyApiClient: PolicyApiClient,
    private val objectMapper: ObjectMapper,
    private val policyElasticSearchService: PolicyElasticSearchService
) {
    companion object {
        private val log = LoggerFactory.getLogger(PolicyFetchService::class.java)
    }

    @Transactional
    @Deprecated("")
    @Throws(IOException::class)
    fun fetchAndSavePolicies(requestDto: PolicyFetchRequestDto?) {

        require(requestDto != null) { "requestDto는 null일 수 없습니다." }

        val pageSize = 100
        var pageNum = 1

        val fetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize)

        val totalCnt = fetchResponse.result?.pagging?.totCount ?: 0
        val totalPages = ceil(totalCnt.toDouble() / pageSize).toInt()

        savePolicies(fetchResponse.result?.youthPolicyList.orEmpty().toMutableList())

        pageNum = 2
        while (pageNum <= totalPages) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            val nextFetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize)
            savePolicies(nextFetchResponse.result?.youthPolicyList.orEmpty().toMutableList())
            pageNum++
        }

        policyElasticSearchService.reindexAllFromDb()
    }

    private fun savePolicies(items: MutableList<PolicyItem?>) {
        val pagePlcyNos = items.mapNotNull { it?.plcyNo }.toSet()
        val existingPlcyNos = policyRepository.findExistingPlcyNos(pagePlcyNos)

        val policies = items
            .filterNotNull()
            .filter { item -> !existingPlcyNos.contains(item.plcyNo) }
            .map { item -> toEntity(item) }

        if (policies.isEmpty()) {
            log.info("저장할 신규 정책 없음 (페이지 스킵)")
            return
        }

        policyRepository.saveAll(policies)
    }

    private fun toEntity(item: PolicyItem): Policy {
        return try {
            Policy(
                plcyNo = item.plcyNo,
                plcyNm = item.plcyNm,
                plcyKywdNm = item.plcyKywdNm,
                plcyExplnCn = item.plcyExplnCn,
                plcySprtCn = item.plcySprtCn,
                sprvsnInstCdNm = item.sprvsnInstCdNm,
                operInstCdNm = item.operInstCdNm,
                aplyPrdSeCd = item.aplyPrdSeCd,
                bizPrdBgngYmd = item.bizPrdBgngYmd,
                bizPrdEndYmd = item.bizPrdEndYmd,
                plcyAplyMthdCn = item.plcyAplyMthdCn,
                aplyUrlAddr = item.aplyUrlAddr,
                sbmsnDcmntCn = item.sbmsnDcmntCn,
                sprtTrgtMinAge = item.sprtTrgtMinAge,
                sprtTrgtMaxAge = item.sprtTrgtMaxAge,
                sprtTrgtAgeLmtYn = item.sprtTrgtAgeLmtYn,
                mrgSttsCd = item.mrgSttsCd,
                earnCndSeCd = item.earnCndSeCd,
                earnMinAmt = item.earnMinAmt,
                earnMaxAmt = item.earnMaxAmt,
                zipCd = item.zipCd,
                jobCd = item.jobCd,
                schoolCd = item.schoolCd,
                aplyYmd = item.aplyYmd,
                sBizCd = item.sbizCd,
                rawJson = objectMapper.writeValueAsString(item)
            )
        } catch (e: Exception) {
            throw RuntimeException("Entity 변환 실패", e)
        }
    }
}
