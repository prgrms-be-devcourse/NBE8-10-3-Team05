package com.back.domain.welfare.policy.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import co.elastic.clients.elasticsearch.indices.RefreshRequest
import com.back.domain.welfare.policy.dto.PolicySearchRequestDto
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.domain.welfare.policy.search.PolicySearchCondition
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisabledIfEnvironmentVariable(
    named = "CI",
    matches = "true",
    disabledReason = "성능 테스트는 CI 환경에서 실행하지 않습니다"
)
@DisabledIfEnvironmentVariable(
    named = "GITHUB_ACTIONS",
    matches = "true",
    disabledReason = "성능 테스트는 GitHub Actions에서 실행하지 않습니다"
)
@DisabledIfSystemProperty(named = "ci", matches = "true", disabledReason = "성능 테스트는 CI 환경에서 실행하지 않습니다")
@TestPropertySource(
    properties = [
        "logging.level.root=WARN",
        "logging.level.org.springframework=WARN",
        "logging.level.org.hibernate=WARN",
        "logging.level.org.hibernate.orm.jdbc=OFF",
        "logging.level.org.elasticsearch=WARN"
    ]
)
@DisplayName("Policy 검색 성능 비교 테스트 (DB vs ElasticSearch)")
internal class PolicyPerformanceComparisonTest {

    @Autowired
    private val policyService: PolicyService? = null

    @Autowired
    private val policyElasticSearchService: PolicyElasticSearchService? = null

    @Autowired
    private val policyRepository: PolicyRepository? = null

    @Autowired
    private val elasticsearchClient: ElasticsearchClient? = null

    private var elasticsearchAvailable = false
    private var testDataCount = 0

    @BeforeEach
    @Transactional
    @Throws(Exception::class)
    fun setUp() {
        println("\n========== 성능 테스트 시작 ==========")

        try {
            elasticsearchAvailable = elasticsearchClient!!.ping().value()
            if (!elasticsearchAvailable) {
                println("⚠️ Elasticsearch 서버가 실행 중이지 않습니다.")
                return
            }
            println("✅ Elasticsearch 연결 성공")
        } catch (e: Exception) {
            println("⚠️ Elasticsearch 연결 실패: " + e.message)
            elasticsearchAvailable = false
            return
        }

        println("🧹 전체 인덱스 정리")
        try {
            val response = elasticsearchClient.cat().indices()
            for (index in response.valueBody()) {
                val indexName = index.index()
                if (indexName != null && indexName.startsWith("policy")) {
                    try {
                        elasticsearchClient.indices()
                            .delete(DeleteIndexRequest.of(Function { d: DeleteIndexRequest.Builder? ->
                                d!!.index(indexName)
                            }))
                        println("  - 삭제: $indexName")
                    } catch (e: Exception) {
                        // 무시
                    }
                }
            }
            Thread.sleep(2000)
        } catch (e: Exception) {
            println("  - 인덱스 정리 실패 (무시): " + e.message)
        }

        println("🧹 DB 정리")
        policyRepository!!.deleteAll()
        policyRepository.flush()

        testDataCount = System.getProperty("test.data.count", "100").toInt()
        println("📝 테스트 데이터 생성: ${testDataCount}건")
        createTestData(testDataCount)

        println("📝 인덱스 생성")
        policyElasticSearchService!!.ensureIndex()
        waitForIndexCreation()

        println("📝 Elasticsearch 인덱싱")
        policyElasticSearchService.reindexAllFromDb()
        waitForIndexing(testDataCount.toLong())

        println("✅ 준비 완료\n")
    }

    @AfterEach
    @Throws(Exception::class)
    fun tearDown() {
        if (!elasticsearchAvailable) return

        try {
            val response = elasticsearchClient!!.cat().indices()
            for (index in response.valueBody()) {
                val indexName = index.index()
                if (indexName != null && indexName.startsWith("policy")) {
                    try {
                        elasticsearchClient.indices()
                            .delete(DeleteIndexRequest.of(Function { d: DeleteIndexRequest.Builder? ->
                                d!!.index(indexName)
                            }))
                    } catch (e: Exception) {
                        // 무시
                    }
                }
            }
            Thread.sleep(500)
        } catch (e: Exception) {
            // 무시
        }
    }

    @Throws(Exception::class)
    private fun waitForIndexCreation() {
        for (i in 0..29) {
            try {
                if (elasticsearchClient!!.indices()
                        .exists(Function { e: ExistsRequest.Builder? -> e!!.index(INDEX) })
                        .value()
                ) {
                    println("  - 인덱스 생성 확인")
                    Thread.sleep(500)
                    return
                }
            } catch (e: Exception) {
                // 계속 시도
            }
            Thread.sleep(200)
        }
        throw AssertionError("❌ 인덱스 생성 실패")
    }

    @Throws(Exception::class)
    private fun waitForIndexing(expectedCount: Long) {
        println("  - 인덱싱 대기: ${expectedCount}건")

        elasticsearchClient!!.indices().refresh(Function { r: RefreshRequest.Builder? -> r!!.index(INDEX) })

        var lastCount: Long = -1
        for (attempt in 0..<MAX_WAIT_ATTEMPTS) {
            try {
                val count = elasticsearchClient
                    .count(CountRequest.of(Function { c: CountRequest.Builder? -> c!!.index(INDEX) }))
                    .count()

                if (count != lastCount && attempt % 10 == 0) {
                    println("    현재: $count / $expectedCount")
                    lastCount = count
                }

                if (count >= expectedCount) {
                    println("  - 인덱싱 완료: ${count}건")
                    Thread.sleep(1000)
                    return
                }

                if (attempt > 0 && attempt % 10 == 0) {
                    elasticsearchClient.indices().refresh(Function { r: RefreshRequest.Builder? -> r!!.index(INDEX) })
                }
            } catch (e: Exception) {
                if (attempt % 10 == 0) println("    에러: " + e.message)
            }

            Thread.sleep(WAIT_INTERVAL_MS)
        }

        throw AssertionError("❌ 인덱싱 타임아웃: ${expectedCount}건 대기 실패")
    }

    @Test
    @DisplayName("나이 조건 검색 성능 비교")
    fun comparePerformance_byAge() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        val dbRequest = PolicySearchRequestDto(25, 35, null, null, null, null, null)
        val esCondition = PolicySearchCondition(age = 30)

        val dbResult = measurePerformance(Supplier { policyService!!.search(dbRequest) })
        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        printComparisonResult("나이 조건 검색", dbResult, esResult)
    }

    @Test
    @DisplayName("소득 조건 검색 성능 비교")
    fun comparePerformance_byEarn() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        val dbRequest = PolicySearchRequestDto(null, null, null, null, null, 2000, 4000)
        val esCondition = PolicySearchCondition(earn = 3000)

        val dbResult = measurePerformance(Supplier { policyService!!.search(dbRequest) })
        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        printComparisonResult("소득 조건 검색", dbResult, esResult)
    }

    @Test
    @DisplayName("지역 코드 검색 성능 비교")
    fun comparePerformance_byRegion() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        val dbRequest = PolicySearchRequestDto(null, null, "11", null, null, null, null)
        val esCondition = PolicySearchCondition(regionCode = "11")

        val dbResult = measurePerformance(Supplier { policyService!!.search(dbRequest) })
        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        printComparisonResult("지역 코드 검색", dbResult, esResult)
    }

    @Test
    @DisplayName("키워드 검색 성능 비교 (ES만 지원)")
    fun comparePerformance_byKeyword() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        val esCondition = PolicySearchCondition(keyword = "청년")

        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        println("=".repeat(80))
        println("키워드 검색 (ES 전용 기능)")
        println("  결과 수: ${esResult.resultCount}")
        println("  평균 응답 시간: ${esResult.averageTime}ms")
        println("  중간값: ${esResult.medianTime}ms")
        println("  최소/최대: ${esResult.minTime}/${esResult.maxTime}ms")
        println("=".repeat(80))
    }

    @Test
    @DisplayName("복합 조건 검색 성능 비교")
    fun comparePerformance_byMultipleConditions() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        val dbRequest = PolicySearchRequestDto(20, 39, "11", null, null, 0, 5000)
        val esCondition = PolicySearchCondition(age = 25, regionCode = "11", earn = 3000)

        val dbResult = measurePerformance(Supplier { policyService!!.search(dbRequest) })
        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        printComparisonResult("복합 조건 검색", dbResult, esResult)
    }

    @Test
    @DisplayName("전체 검색 성능 비교 (조건 없음)")
    fun comparePerformance_all() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        val dbRequest = PolicySearchRequestDto(null, null, null, null, null, null, null)
        val esCondition = PolicySearchCondition()

        val dbResult = measurePerformance(Supplier { policyService!!.search(dbRequest) })
        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        printComparisonResult("전체 검색", dbResult, esResult)
    }

    @Test
    @DisplayName("데이터 양에 따른 성능 비교 (100 vs 1000 vs 10000)")
    fun comparePerformance_byDataSize() {
        Assumptions.assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다")

        println("=".repeat(80))
        println("데이터 양에 따른 성능 테스트")
        println("현재 데이터: ${testDataCount}건")
        println("더 많은 데이터로 테스트하려면: -Dtest.data.count=1000")
        println("=".repeat(80))

        val esCondition = PolicySearchCondition(age = 30)

        val esResult = measurePerformance(Supplier {
            try { policyElasticSearchService!!.search(esCondition, 0, 100) }
            catch (e: IOException) { throw RuntimeException(e) }
        })

        println("ES 검색 성능 (${testDataCount}건)")
        println("  평균: ${esResult.averageTime}ms")
        println("  중간값: ${esResult.medianTime}ms")
    }

    // ========== Helper Methods ==========

    private fun createTestData(count: Int) {
        val policies = mutableListOf<Policy>()

        for (i in 0..<count) {
            val uniqueId = UUID.randomUUID().toString().substring(0, 8)
            val minAge = if (i % 10 == 0) 25 else 20 + (i % 50)
            val maxAge = if (i % 10 == 0) 35 else 40 + (i % 30)

            policies.add(
                Policy(
                    plcyNo = "PERF-$i-$uniqueId",
                    plcyNm = "정책 $i",
                    sprtTrgtMinAge = minAge.toString(),
                    sprtTrgtMaxAge = maxAge.toString(),
                    sprtTrgtAgeLmtYn = "Y",
                    earnCndSeCd = "연소득",
                    earnMinAmt = ((i % 10) * 1000).toString(),
                    earnMaxAmt = ((i % 10 + 1) * 1000).toString(),
                    zipCd = (11 + (i % 17)).toString(),
                    jobCd = "J${String.format("%02d", i % 10)}",
                    schoolCd = "S${String.format("%02d", i % 5)}",
                    mrgSttsCd = if (i % 2 == 0) "Y" else "N",
                    plcyKywdNm = "${if (i % 2 == 0) "청년" else "중장년"},지원",
                    plcyExplnCn = "정책 설명 $i"
                )
            )
        }

        policyRepository!!.saveAll(policies)
        policyRepository.flush()
    }

    private fun measurePerformance(supplier: Supplier<*>): PerformanceResult {
        repeat(WARMUP_ITERATIONS) { supplier.get() }

        val times = mutableListOf<Long>()
        var resultCount = 0

        for (i in 0..<TEST_ITERATIONS) {
            val start = System.nanoTime()
            val results = supplier.get()
            val end = System.nanoTime()

            times.add(TimeUnit.NANOSECONDS.toMillis(end - start))
            if (i == 0 && results is MutableList<*>) resultCount = results.size
        }

        return PerformanceResult(times, resultCount)
    }

    private fun printComparisonResult(testName: String, dbResult: PerformanceResult, esResult: PerformanceResult) {
        println("=".repeat(80))
        println(testName)
        println("-".repeat(80))
        println("DB 검색:")
        println("  결과 수: ${dbResult.resultCount}")
        println("  평균: ${dbResult.averageTime}ms")
        println("  중간값: ${dbResult.medianTime}ms")
        println("  최소/최대: ${dbResult.minTime}/${dbResult.maxTime}ms")
        println()
        println("ES 검색:")
        println("  결과 수: ${esResult.resultCount}")
        println("  평균: ${esResult.averageTime}ms")
        println("  중간값: ${esResult.medianTime}ms")
        println("  최소/최대: ${esResult.minTime}/${esResult.maxTime}ms")
        println()
        val improvement = ((dbResult.averageTime - esResult.averageTime).toDouble() / dbResult.averageTime) * 100
        println("성능 차이: ${String.format("%.2f%%", improvement)}${if (improvement > 0) " (ES가 빠름)" else " (DB가 빠름)"}")
        println("=".repeat(80))
    }

    private fun interface Supplier<T> {
        fun get(): T?
    }

    private class PerformanceResult(times: MutableList<Long>, val resultCount: Int) {
        private val times: MutableList<Long> = times.sorted().toMutableList()

        val averageTime: Long get() = times.average().toLong()
        val medianTime: Long get() = times[times.size / 2]
        val minTime: Long get() = times[0]
        val maxTime: Long get() = times[times.size - 1]
    }

    companion object {
        private const val INDEX = "policy"
        private const val WARMUP_ITERATIONS = 3
        private const val TEST_ITERATIONS = 10
        private const val MAX_WAIT_ATTEMPTS = 60
        private const val WAIT_INTERVAL_MS: Long = 300
    }
}
