package com.back.domain.welfare.policy.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final PolicyRepository policyRepository;
    private final PolicyApiClient policyApiClient;
    private final ObjectMapper objectMapper;
    private final PolicyElasticSearchService policyElasticSearchService;

    @Transactional
    @Deprecated
    public void fetchAndSavePolicies(PolicyFetchRequestDto requestDto) throws IOException {

        int pageSize = 100;
        int pageNum = 1;

        // 1페이지 호출
        PolicyFetchResponseDto fetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize);

        int totalCnt = fetchResponse.result().pagging().totCount();
        int totalPages = (int) Math.ceil((double) totalCnt / pageSize);

        // 1페이지 저장
        savePolicies(fetchResponse.result().youthPolicyList());

        // 2페이지 이상 반복
        for (pageNum = 2; pageNum <= totalPages; pageNum++) {
            try {
                Thread.sleep(500); // API 과부하 방지
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PolicyFetchResponseDto nextFetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize);
            savePolicies(nextFetchResponse.result().youthPolicyList());
        }

        policyElasticSearchService.reindexAllFromDb();
    }

    private void savePolicies(List<PolicyFetchResponseDto.PolicyItem> items) {
        // 1. 페이지 내 plcyNo 수집
        Set<String> pagePlcyNos = items.stream().map(PolicyItem::plcyNo).collect(Collectors.toSet());

        // 2. DB에 이미 존재하는 plcyNo 조회
        Set<String> existingPlcyNos = policyRepository.findExistingPlcyNos(pagePlcyNos);

        // 3. 페이지 내 중복 + DB 중복 제거
        List<Policy> policies = items.stream()
                .filter(item -> !existingPlcyNos.contains(item.plcyNo()))
                .map(this::toEntity)
                .toList();

        if (policies.isEmpty()) {
            log.info("저장할 신규 정책 없음 (페이지 스킵)");
            return;
        }

        policyRepository.saveAll(policies);
    }

    private Policy toEntity(PolicyFetchResponseDto.PolicyItem item) {
        try {
            return Policy.builder()
                    .plcyNo(item.plcyNo())
                    .plcyNm(item.plcyNm())
                    .plcyKywdNm(item.plcyKywdNm())
                    .plcyExplnCn(item.plcyExplnCn())
                    .plcySprtCn(item.plcySprtCn())
                    .sprvsnInstCdNm(item.sprvsnInstCdNm())
                    .operInstCdNm(item.operInstCdNm())
                    .aplyPrdSeCd(item.aplyPrdSeCd())
                    .bizPrdBgngYmd(item.bizPrdBgngYmd())
                    .bizPrdEndYmd(item.bizPrdEndYmd())
                    .plcyAplyMthdCn(item.plcyAplyMthdCn())
                    .aplyUrlAddr(item.aplyUrlAddr())
                    .sbmsnDcmntCn(item.sbmsnDcmntCn())
                    .sprtTrgtMinAge(item.sprtTrgtMinAge())
                    .sprtTrgtMaxAge(item.sprtTrgtMaxAge())
                    .sprtTrgtAgeLmtYn(item.sprtTrgtAgeLmtYn())
                    .mrgSttsCd(item.mrgSttsCd())
                    .earnCndSeCd(item.earnCndSeCd())
                    .earnMinAmt(item.earnMinAmt())
                    .earnMaxAmt(item.earnMaxAmt())
                    .zipCd(item.zipCd())
                    .jobCd(item.jobCd())
                    .schoolCd(item.schoolCd())
                    .aplyYmd(item.aplyYmd())
                    .sBizCd(item.sbizCd())
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Entity 변환 실패", e);
        }
    }
}
