package com.back.domain.welfare.policy.controller

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.HealthStatus
import co.elastic.clients.elasticsearch._types.Time
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.cluster.HealthRequest
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import co.elastic.clients.elasticsearch.indices.RefreshRequest
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.domain.welfare.policy.service.PolicyElasticSearchService
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import org.hamcrest.Matchers
import java.util.*
import java.util.function.Function

@ActiveProfiles("test")
@TestPropertySource(properties = ["app.elasticsearch.policy-index=policy_controller"])
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolicyControllerTest {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Autowired
    private val policyRepository: PolicyRepository? = null

    @Autowired
    private val policyElasticSearchService: PolicyElasticSearchService? = null

    @Autowired
    private val elasticsearchClient: ElasticsearchClient? = null

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        // 1️⃣ ES 서버 살아있는지만 확인
        try {
            Assumptions.assumeTrue(elasticsearchClient!!.ping().value(), "Elasticsearch 서버가 없어서 테스트 스킵")
        } catch (e: Exception) {
            Assumptions.assumeTrue(false, "Elasticsearch 연결 실패 → 테스트 스킵")
        }

        // 2️⃣ index 정리
        cleanupElasticsearch()

        // 3️⃣ DB 정리
        policyRepository!!.deleteAll()
        policyRepository.flush()

        // 4️⃣ 테스트 데이터 생성
        val policy = Policy.builder()
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
            .build()

        policyRepository.saveAndFlush(policy)

        // 5️⃣ index 생성 보장
        policyElasticSearchService!!.ensureIndex()

        elasticsearchClient!!
            .cluster()
            .health(Function { h: HealthRequest.Builder? ->
                h!!.index(INDEX).waitForStatus(HealthStatus.Yellow).timeout(
                    Function { t: Time.Builder? -> t!!.time("5s") })
            })

        // 6️⃣ reindex
        policyElasticSearchService.reindexAllFromDb()

        try {
            elasticsearchClient.indices().refresh(Function { r: RefreshRequest.Builder? -> r!!.index(INDEX) })
        } catch (e: ElasticsearchException) {
            if (e.message!!.contains("index_not_found_exception")) {
                // 인덱스 생성 후 재시도
                elasticsearchClient.indices().create(Function { c: CreateIndexRequest.Builder? -> c!!.index(INDEX) })
            }
        }
    }

    @Throws(Exception::class)
    private fun cleanupElasticsearch() {
        if (elasticsearchClient!!.indices().exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                .value()
        ) {
            elasticsearchClient.deleteByQuery(Function { d: DeleteByQueryRequest.Builder? ->
                d!!.index(INDEX).query(
                    Function { q: Query.Builder? -> q!!.matchAll(Function { m: MatchAllQuery.Builder? -> m }) })
            })
            // 삭제 후 즉시 반영을 위해 refresh
            elasticsearchClient.indices().refresh(Function { r: RefreshRequest.Builder? -> r!!.index(INDEX) })
        }
    }

    @Test
    @DisplayName("GET /api/v1/policy/search - 조건 기반 검색")
    @Throws(Exception::class)
    fun search_policy_with_conditions() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/api/v1/welfare/policy/search")
                .param("keyword", "청년")
                .param("age", "25")
                .param("regionCode", "11")
                .param("jobCode", "J01")
                .param("marriageStatus", "N")
                .param("keywords", "주거")
                .param("from", "0")
                .param("size", "10")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].plcyNm").value("청년 주거 지원 컨트롤러 테스트"))
    }

    @Test
    @DisplayName("GET /api/v1/policy/search - 조건 없이 전체 검색")
    @Throws(Exception::class)
    fun search_policy_without_conditions() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/api/v1/welfare/policy/search").param("from", "0").param("size", "10")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(Matchers.greaterThanOrEqualTo(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].plcyNm").value(Matchers.hasItem("청년 주거 지원 컨트롤러 테스트")))
    }

    companion object {
        private const val INDEX = "policy_controller"
    }
}
