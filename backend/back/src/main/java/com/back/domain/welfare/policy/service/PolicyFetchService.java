package com.back.domain.welfare.policy.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
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

        int totalCnt = fetchResponse.result().getPagging().getTotCount();
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

    private void savePolicies(List<PolicyItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        // null 요소 방어 (혹시 모를 API 오류 대비)
        List<PolicyItem> safeItems = items.stream()
                .filter(Objects::nonNull)
                .toList();

        // 1. 페이지 내 plcyNo 수집
        Set<String> pagePlcyNos = safeItems.stream()
                .map(PolicyItem::getPlcyNo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. DB에 이미 존재하는 plcyNo 조회
        Set<String> existingPlcyNos = policyRepository.findExistingPlcyNos(pagePlcyNos);

        // 3. 페이지 내 중복 + DB 중복 제거
        List<Policy> policies = safeItems.stream()
                .filter(item -> item.getPlcyNo() != null)
                .filter(item -> !existingPlcyNos.contains(item.getPlcyNo()))
                .map(this::toEntity)
                .toList();

        if (policies.isEmpty()) {
            log.info("저장할 신규 정책 없음 (페이지 스킵)");
            return;
        }

        policyRepository.saveAll(policies);
    }

    private Policy toEntity(PolicyItem item) {
        try {
            return Policy.builder()
                    .plcyNo(item.getPlcyNo())
                    .plcyNm(item.getPlcyNm())
                    .plcyKywdNm(item.getPlcyKywdNm())
                    .plcyExplnCn(item.getPlcyExplnCn())
                    .plcySprtCn(item.getPlcySprtCn())
                    .sprvsnInstCdNm(item.getSprvsnInstCdNm())
                    .operInstCdNm(item.getOperInstCdNm())
                    .aplyPrdSeCd(item.getAplyPrdSeCd())
                    .bizPrdBgngYmd(item.getBizPrdBgngYmd())
                    .bizPrdEndYmd(item.getBizPrdEndYmd())
                    .plcyAplyMthdCn(item.getPlcyAplyMthdCn())
                    .aplyUrlAddr(item.getAplyUrlAddr())
                    .sbmsnDcmntCn(item.getSbmsnDcmntCn())
                    .sprtTrgtMinAge(item.getSprtTrgtMinAge())
                    .sprtTrgtMaxAge(item.getSprtTrgtMaxAge())
                    .sprtTrgtAgeLmtYn(item.getSprtTrgtAgeLmtYn())
                    .mrgSttsCd(item.getMrgSttsCd())
                    .earnCndSeCd(item.getEarnCndSeCd())
                    .earnMinAmt(item.getEarnMinAmt())
                    .earnMaxAmt(item.getEarnMaxAmt())
                    .zipCd(item.getZipCd())
                    .jobCd(item.getJobCd())
                    .schoolCd(item.getSchoolCd())
                    .aplyYmd(item.getAplyYmd())
                    .sBizCd(item.getSbizCd())
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Entity 변환 실패", e);
        }
    }
}