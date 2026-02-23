package com.back.domain.welfare.policy.controller;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.service.PolicyElasticSearchService;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PolicyControllerTest {

    private static final String INDEX = "policy";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyElasticSearchService policyElasticSearchService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @BeforeEach
    void setUp() throws Exception {
        // 1️⃣ ES 서버 살아있는지만 확인
        try {
            assumeTrue(elasticsearchClient.ping().value(), "Elasticsearch 서버가 없어서 테스트 스킵");
        } catch (Exception e) {
            assumeTrue(false, "Elasticsearch 연결 실패 → 테스트 스킵");
        }

        // 2️⃣ index 정리
        cleanupElasticsearch();

        // 3️⃣ DB 정리
        policyRepository.deleteAll();
        policyRepository.flush();

        // 4️⃣ 테스트 데이터 생성
        Policy policy = Policy.builder()
                .plcyNo("API-" + UUID.randomUUID())
                .plcyNm("청년 주거 지원 컨트롤러 테스트")
                .sprtTrgtMinAge("20")
                .sprtTrgtMaxAge("39")
                .sprtTrgtAgeLmtYn("Y")
                .zipCd("11")
                .jobCd("J01")
                .schoolCd("S01")
                .mrgSttsCd("N")
                .plcyKywdNm("청년,주거")
                .plcyExplnCn("컨트롤러 테스트용 정책 설명")
                .build();

        policyRepository.saveAndFlush(policy);

        // 5️⃣ index 생성 보장
        policyElasticSearchService.ensureIndex();

        // 6️⃣ reindex
        policyElasticSearchService.reindexAllFromDb();

        elasticsearchClient.indices().refresh(r -> r.index(INDEX));
    }

    private void cleanupElasticsearch() throws Exception {
        if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
            elasticsearchClient.indices().delete(d -> d.index(INDEX));
            int retry = 0;
            while (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value() && retry < 10) {
                Thread.sleep(500);
                retry++;
            }
        }
    }

    @Test
    @DisplayName("GET /api/v1/policy/search - 조건 기반 검색")
    void search_policy_with_conditions() throws Exception {
        mockMvc.perform(get("/api/v1/welfare/policy/search")
                        .param("keyword", "청년")
                        .param("age", "25")
                        .param("regionCode", "11")
                        .param("jobCode", "J01")
                        .param("marriageStatus", "N")
                        .param("keywords", "주거")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].plcyNm").value("청년 주거 지원 컨트롤러 테스트"));
    }

    @Test
    @DisplayName("GET /api/v1/policy/search - 조건 없이 전체 검색")
    void search_policy_without_conditions() throws Exception {
        mockMvc.perform(get("/api/v1/welfare/policy/search").param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
