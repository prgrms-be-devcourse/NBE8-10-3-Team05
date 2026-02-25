package com.back.domain.welfare.policy.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import co.elastic.clients.elasticsearch.indices.RefreshRequest
import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.entity.Policy.Companion.builder
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.domain.welfare.policy.search.PolicySearchCondition
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Order(1)
@DisplayName("PolicyElasticSearchService 통합 테스트")
internal class PolicyElasticSearchServiceIntegrationTest {
    @Autowired
    private val policyElasticSearchService: PolicyElasticSearchService? = null

    @Autowired
    private val policyRepository: PolicyRepository? = null

    @Autowired
    var policyDocumentMapper: PolicyDocumentMapper? = null

    @Autowired
    private val elasticsearchClient: ElasticsearchClient? = null

    private var elasticsearchAvailable = false

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        try {
            elasticsearchAvailable = elasticsearchClient!!.ping().value()
            if (!elasticsearchAvailable) {
                println("⚠️ Elasticsearch 서버가 실행 중이지 않습니다.")
                return
            }
        } catch (e: Exception) {
            println("⚠️ Elasticsearch 연결 실패: " + e.message)
            elasticsearchAvailable = false
            return
        }

        println("🧹 전체 Elasticsearch 정리 시작")

        try {
            val response = elasticsearchClient.cat().indices()
            var deletedCount = 0
            for (index in response.valueBody()) {
                val indexName = index.index()
                if (indexName != null && indexName.startsWith("policy")) {
                    try {
                        elasticsearchClient.indices()
                            .delete(DeleteIndexRequest.of(Function { d: DeleteIndexRequest.Builder? ->
                                d!!.index(indexName)
                            }))
                        deletedCount++
                        println("  - 삭제: " + indexName)
                    } catch (e: Exception) {
                        println("  - 삭제 실패 (무시): " + indexName)
                    }
                }
            }
            println("  - 총 " + deletedCount + "개 인덱스 삭제")

            if (deletedCount > 0) {
                Thread.sleep(2000)
            }
        } catch (e: Exception) {
            println("  - 인덱스 목록 조회 실패: " + e.message)
        }

        println("🧹 DB 정리")
        policyRepository!!.deleteAll()
        policyRepository.flush()

        println("✅ 정리 완료\n")
    }

    @AfterEach
    @Throws(Exception::class)
    fun tearDown() {
        if (!elasticsearchAvailable) return

        try {
            val exists = elasticsearchClient!!.indices()
                .exists { it.index(INDEX) }
                .value()
            if (exists) {
                elasticsearchClient.indices().delete { it.index(INDEX) }
            }
        } catch (e: Exception) {
            // 무시
        }
    }
    @Throws(Exception::class)
    private fun cleanupElasticsearch() {
        try {
            if (elasticsearchClient!!.indices().exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                    .value()
            ) {
                elasticsearchClient.indices()
                    .delete(DeleteIndexRequest.of(Function { d: DeleteIndexRequest.Builder? ->
                        d!!.index(INDEX)
                    }))

                for (i in 0..19) {
                    try {
                        if (!elasticsearchClient
                                .indices()
                                .exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                                .value()
                        ) {
                            break
                        }
                    } catch (e: Exception) {
                        break
                    }
                    Thread.sleep(200)
                }
            }
        } catch (e: Exception) {
            // 인덱스가 없으면 무시
        }
    }

    @Throws(Exception::class)
    private fun waitForIndexing(expectedCount: Long) {
        println("🔍 인덱싱 대기 시작: 예상 문서 수 = " + expectedCount)

        elasticsearchClient!!.indices().refresh(Function { r: RefreshRequest.Builder? -> r!!.index(INDEX) })

        var lastCount: Long = 0
        for (attempt in 0..<MAX_WAIT_ATTEMPTS) {
            try {
                val count = elasticsearchClient
                    .count(CountRequest.of(Function { c: CountRequest.Builder? -> c!!.index(INDEX) }))
                    .count()

                lastCount = count

                if (count >= expectedCount) {
                    val searchResponse = elasticsearchClient.search<PolicyDocument?>(
                        Function { s: SearchRequest.Builder? ->
                            s!!.index(INDEX).query(Function { q: Query.Builder? ->
                                q!!.matchAll(
                                    Function { m: MatchAllQuery.Builder? -> m })
                            }).size(expectedCount.toInt())
                        },
                        PolicyDocument::class.java
                    )

                    val searchCount = searchResponse.hits().total()!!.value()
                    if (searchCount >= expectedCount) {
                        println("✅ 인덱싱 완료: " + searchCount + "건 (시도: " + (attempt + 1) + ")")
                        Thread.sleep(500)
                        return
                    }
                }

                if (attempt % 10 == 0 && attempt > 0) {
                    println("⏳ 대기 중... " + count + " / " + expectedCount + " (시도: " + (attempt + 1) + ")")
                    elasticsearchClient.indices().refresh(Function { r: RefreshRequest.Builder? -> r!!.index(INDEX) })
                }
            } catch (e: Exception) {
                if (attempt % 10 == 0 && attempt > 0) {
                    println("⚠️ 검색 실패 (시도: " + (attempt + 1) + "): " + e.message)
                }
            }

            Thread.sleep(WAIT_INTERVAL_MS)
        }

        throw AssertionError("⚠️ 타임아웃: " + expectedCount + "건 인덱싱 대기 실패 (마지막 확인: " + lastCount + "건)")
    }

    @Nested
    @DisplayName("인덱스 관리")
    internal inner class IndexManagement {
        @Test
        @DisplayName("ensureIndex: 인덱스가 없으면 생성")
        @Throws(Exception::class)
        fun ensureIndex_createsIndexWhenNotExists() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            policyElasticSearchService!!.ensureIndex()
            Thread.sleep(1000)

            val exists =
                elasticsearchClient!!.indices().exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                    .value()
            Assertions.assertThat(exists).isTrue()
        }

        @Test
        @DisplayName("ensureIndex: 인덱스가 이미 있으면 재생성하지 않음")
        @Throws(Exception::class)
        fun ensureIndex_doesNotRecreateWhenExists() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            policyElasticSearchService!!.ensureIndex()
            Thread.sleep(1000)

            val firstExists =
                elasticsearchClient!!.indices().exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                    .value()
            Assertions.assertThat(firstExists).isTrue()

            policyElasticSearchService.ensureIndex()
            Thread.sleep(500)

            val stillExists =
                elasticsearchClient.indices().exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                    .value()
            Assertions.assertThat(stillExists).isTrue()
        }
    }

    @Nested
    @DisplayName("문서 인덱싱")
    internal open inner class DocumentIndexing {
        @Test
        @Transactional
        @DisplayName("reindexAllFromDb: DB의 Policy를 ES에 인덱싱")
        @Throws(Exception::class)
        open fun reindexAllFromDb_indexesAllPolicies() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val uniqueId1 = UUID.randomUUID().toString().substring(0, 8)
            val uniqueId2 = UUID.randomUUID().toString().substring(0, 8)

            val policy1 = builder()
                .plcyNo("TEST-001-" + uniqueId1)
                .plcyNm("청년 주거 지원 정책")
                .sprtTrgtMinAge("20")
                .sprtTrgtMaxAge("39")
                .sprtTrgtAgeLmtYn("Y")
                .earnCndSeCd("연소득")
                .earnMinAmt("0")
                .earnMaxAmt("5000")
                .zipCd("11")
                .jobCd("J01")
                .schoolCd("S01")
                .mrgSttsCd("N")
                .plcyKywdNm("청년,주거,취업")
                .plcyExplnCn("청년을 위한 주거 지원 정책입니다")
                .build()

            val policy2 = builder()
                .plcyNo("TEST-002-" + uniqueId2)
                .plcyNm("중장년 취업 지원")
                .sprtTrgtMinAge("40")
                .sprtTrgtMaxAge("65")
                .sprtTrgtAgeLmtYn("Y")
                .earnCndSeCd("무관")
                .zipCd("11")
                .jobCd("J02")
                .plcyKywdNm("취업,중장년")
                .plcyExplnCn("중장년층 취업을 지원하는 정책입니다")
                .build()

            policyRepository!!.save(policy1)
            policyRepository.save(policy2)
            policyRepository.flush()

            policyElasticSearchService!!.ensureIndex()
            Thread.sleep(500)

            val indexedCount = policyElasticSearchService.reindexAllFromDb()
            waitForIndexing(2)

            Assertions.assertThat(indexedCount).isGreaterThanOrEqualTo(2)

            val searchResponse = elasticsearchClient!!.search<PolicyDocument?>(
                Function { s: SearchRequest.Builder? ->
                    s!!.index(INDEX).query(Function { q: Query.Builder? ->
                        q!!.matchAll(
                            Function { m: MatchAllQuery.Builder? -> m })
                    })
                }, PolicyDocument::class.java
            )

            Assertions.assertThat(searchResponse.hits().total()!!.value()).isGreaterThanOrEqualTo(2)
        }

        @Test
        @Transactional
        @DisplayName("reindexAllFromDb: DB에 데이터가 없으면 0 반환")
        @Throws(IOException::class)
        open fun reindexAllFromDb_returnsZeroWhenNoData() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            policyRepository!!.deleteAll()
            policyRepository.flush()

            val indexedCount = policyElasticSearchService!!.reindexAllFromDb()

            Assertions.assertThat(indexedCount).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("키워드 검색")
    internal open inner class KeywordSearch {
        @BeforeEach
        @Throws(Exception::class)
        open fun setUp() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            policyRepository!!.deleteAll()
            policyRepository.flush()

            val uniqueId1 = UUID.randomUUID().toString().substring(0, 8)
            val uniqueId2 = UUID.randomUUID().toString().substring(0, 8)
            val uniqueId3 = UUID.randomUUID().toString().substring(0, 8)

            val policy1 = builder()
                .plcyNo("SEARCH-001-" + uniqueId1)
                .plcyNm("청년 주거 지원")
                .sprtTrgtMinAge("20")
                .sprtTrgtMaxAge("39")
                .sprtTrgtAgeLmtYn("Y")
                .earnCndSeCd("연소득")
                .earnMinAmt("0")
                .earnMaxAmt("5000")
                .zipCd("11")
                .jobCd("J01")
                .schoolCd("S01")
                .mrgSttsCd("N")
                .plcyKywdNm("청년,주거")
                .plcyExplnCn("청년을 위한 주거 지원 정책")
                .build()

            val policy2 = builder()
                .plcyNo("SEARCH-002-" + uniqueId2)
                .plcyNm("중장년 취업 지원")
                .sprtTrgtMinAge("40")
                .sprtTrgtMaxAge("65")
                .sprtTrgtAgeLmtYn("Y")
                .earnCndSeCd("무관")
                .zipCd("26")
                .jobCd("J02")
                .plcyKywdNm("취업,중장년")
                .plcyExplnCn("중장년 취업을 지원합니다")
                .build()

            val policy3 = builder()
                .plcyNo("SEARCH-003-" + uniqueId3)
                .plcyNm("전체 교육 지원")
                .sprtTrgtMinAge("18")
                .sprtTrgtMaxAge("70")
                .sprtTrgtAgeLmtYn("Y")
                .earnCndSeCd("무관")
                .earnMinAmt("0")
                .earnMaxAmt("3000")
                .zipCd("11")
                .jobCd("J01")
                .schoolCd("S02")
                .plcyKywdNm("교육")
                .plcyExplnCn("모든 연령 교육 지원")
                .build()

            policyRepository.save(policy1)
            policyRepository.save(policy2)
            policyRepository.save(policy3)
            policyRepository.flush()

            policyElasticSearchService!!.ensureIndex()
            Thread.sleep(500)

            policyElasticSearchService.reindexAllFromDb()
            waitForIndexing(3)
        }

        @Test
        @DisplayName("search: 키워드 조건으로 검색")
        @Throws(IOException::class)
        fun search_byKeyword() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(keyword = "청년")

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
            Assertions.assertThat(
                results!!.stream().anyMatch { doc -> doc!!.plcyNm!!.contains("청년") })
                .isTrue()
        }

        @Test
        @DisplayName("search: 나이 조건으로 필터링")
        @Throws(IOException::class)
        fun search_byAge() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(age = 25)

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
            results!!.forEach { doc ->
                if (doc!!.minAge != null && doc.maxAge != null) {
                    Assertions.assertThat(doc.minAge).isLessThanOrEqualTo(25)
                    Assertions.assertThat(doc.maxAge).isGreaterThanOrEqualTo(25)
                }
            }
        }

        @Test
        @DisplayName("search: 소득 조건으로 필터링")
        @Throws(IOException::class)
        fun search_byEarn() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(earn = 3000)

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
            results!!.forEach { doc ->
                if (doc!!.earnMin != null && doc.earnMax != null) {
                    Assertions.assertThat(doc.earnMin).isLessThanOrEqualTo(3000)
                    Assertions.assertThat(doc.earnMax).isGreaterThanOrEqualTo(3000)
                }
            }
        }

        @Test
        @DisplayName("search: 지역 코드로 필터링")
        @Throws(IOException::class)
        fun search_byRegionCode() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(regionCode = "11")

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
            results!!.forEach { doc ->
                if (doc!!.regionCode != null) {
                    Assertions.assertThat(doc.regionCode).isEqualTo("11")
                }
            }
        }

        @Test
        @DisplayName("search: 직업 코드로 필터링")
        @Throws(IOException::class)
        fun search_byJobCode() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(jobCode = "J01")

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
            results!!.forEach { doc ->
                if (doc!!.jobCode != null) {
                    Assertions.assertThat(doc.jobCode).isEqualTo("J01")
                }
            }
        }

        @Test
        @DisplayName("search: 결혼 상태로 필터링")
        @Throws(IOException::class)
        fun search_byMarriageStatus() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(marriageStatus = "N")

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
            results!!.forEach { doc ->
                if (doc!!.marriageStatus != null) {
                    Assertions.assertThat(doc.marriageStatus).isEqualTo("N")
                }
            }
        }

        @Test
        @DisplayName("search: 키워드 태그로 필터링")
        @Throws(IOException::class)
        fun search_byKeywords() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(keywords = mutableListOf("청년", "주거"))

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
        }

        @Test
        @DisplayName("search: 복합 조건 검색")
        @Throws(IOException::class)
        fun search_byMultipleConditions() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(
                keyword = "청년",
                age = 25,
                earn = 3000,
                regionCode = "11",
                jobCode = "J01",
                marriageStatus = "N",
                keywords = mutableListOf("주거")
            )

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
        }

        @Test
        @DisplayName("search: 조건이 없으면 전체 검색")
        @Throws(IOException::class)
        fun search_returnsAllWhenNoCondition() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition()

            val results = policyElasticSearchService!!.search(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(results).isNotEmpty()
        }
    }

    @Nested
    @DisplayName("검색 결과 총 개수 포함")
    internal open inner class SearchWithTotal {
        @BeforeEach
        @Throws(Exception::class)
        open fun setUp() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            policyRepository!!.deleteAll()
            policyRepository.flush()

            for (i in 1..5) {
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val policy = builder()
                    .plcyNo("TOTAL-" + i + "-" + uniqueId)
                    .plcyNm("테스트 정책 " + i)
                    .plcyKywdNm("테스트")
                    .plcyExplnCn("테스트 정책 설명 " + i)
                    .build()
                policyRepository.save(policy)
            }
            policyRepository.flush()

            policyElasticSearchService!!.ensureIndex()
            Thread.sleep(500)

            policyElasticSearchService.reindexAllFromDb()
            waitForIndexing(5)
        }

        @Test
        @DisplayName("searchWithTotal: 문서 목록과 총 개수 반환")
        @Throws(IOException::class)
        fun searchWithTotal_returnsDocumentsAndTotal() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(keyword = "테스트")

            val result =
                policyElasticSearchService!!.searchWithTotal(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(result.getDocuments()).isNotEmpty()
            Assertions.assertThat(result.getTotal()).isGreaterThanOrEqualTo(5)
            Assertions.assertThat(result.getTotal())
                .isGreaterThanOrEqualTo(result.getDocuments().size.toLong())
        }

        @Test
        @DisplayName("searchWithTotal: 페이지네이션 시 총 개수는 전체 개수")
        @Throws(IOException::class)
        fun searchWithTotal_totalIsFullCount() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(keyword = "테스트")

            val page1 = policyElasticSearchService!!.searchWithTotal(condition, 0, 2)
            val page2 = policyElasticSearchService.searchWithTotal(condition, 2, 2)

            Assertions.assertThat(page1.getTotal()).isEqualTo(page2.getTotal())
            Assertions.assertThat(page1.getTotal()).isGreaterThanOrEqualTo(5)
        }

        @Test
        @DisplayName("searchWithTotal: 검색 결과가 없으면 total은 0")
        @Throws(IOException::class)
        fun searchWithTotal_returnsZeroWhenNoResults() {
            Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

            val condition = PolicySearchCondition(keyword = "존재하지않는키워드12345")

            val result =
                policyElasticSearchService!!.searchWithTotal(condition, 0, 10)

            Assertions.assertThat<PolicyDocument?>(result.getDocuments()).isEmpty()
            Assertions.assertThat(result.getTotal()).isEqualTo(0)
        }
    }

    companion object {
        private const val INDEX = "policy"
        private const val MAX_WAIT_ATTEMPTS = 60
        private const val WAIT_INTERVAL_MS: Long = 300
    }
}
