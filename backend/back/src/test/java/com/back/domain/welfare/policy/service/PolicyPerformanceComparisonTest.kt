package com.back.domain.welfare.policy.service;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.search.PolicySearchCondition;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// CI 환경에서는 이 테스트 클래스 전체를 실행하지 않음
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "성능 테스트는 CI 환경에서 실행하지 않습니다")
// GitHub Actions 환경에서도 비활성화
@DisabledIfEnvironmentVariable(
        named = "GITHUB_ACTIONS",
        matches = "true",
        disabledReason = "성능 테스트는 GitHub Actions에서 실행하지 않습니다")
// Gradle CI 환경에서도 비활성화
@DisabledIfSystemProperty(named = "ci", matches = "true", disabledReason = "성능 테스트는 CI 환경에서 실행하지 않습니다")
@TestPropertySource(
        properties = {
            "logging.level.root=WARN",
            "logging.level.org.springframework=WARN",
            "logging.level.org.hibernate=WARN",
            "logging.level.org.hibernate.orm.jdbc=OFF",
            "logging.level.org.elasticsearch=WARN"
        })
@DisplayName("Policy 검색 성능 비교 테스트 (DB vs ElasticSearch)")
class PolicyPerformanceComparisonTest {

    private static final String INDEX = "policy";
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 10;
    private static final int MAX_WAIT_ATTEMPTS = 60;
    private static final long WAIT_INTERVAL_MS = 300;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyElasticSearchService policyElasticSearchService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private boolean elasticsearchAvailable = false;
    private int testDataCount = 0;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        System.out.println("\n========== 성능 테스트 시작 ==========");

        // Elasticsearch 서버 연결 확인
        try {
            elasticsearchAvailable = elasticsearchClient.ping().value();
            if (!elasticsearchAvailable) {
                System.out.println("⚠️ Elasticsearch 서버가 실행 중이지 않습니다.");
                return;
            }
            System.out.println("✅ Elasticsearch 연결 성공");
        } catch (Exception e) {
            System.out.println("⚠️ Elasticsearch 연결 실패: " + e.getMessage());
            elasticsearchAvailable = false;
            return;
        }

        // 모든 policy* 인덱스 정리
        System.out.println("🧹 전체 인덱스 정리");
        try {
            var response = elasticsearchClient.cat().indices();
            for (var index : response.valueBody()) {
                String indexName = index.index();
                if (indexName != null && indexName.startsWith("policy")) {
                    try {
                        elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(indexName)));
                        System.out.println("  - 삭제: " + indexName);
                    } catch (Exception e) {
                        // 무시
                    }
                }
            }
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("  - 인덱스 정리 실패 (무시): " + e.getMessage());
        }

        // DB 데이터 정리
        System.out.println("🧹 DB 정리");
        policyRepository.deleteAll();
        policyRepository.flush();

        // 테스트 데이터 생성
        testDataCount = Integer.parseInt(System.getProperty("test.data.count", "100"));
        System.out.println("📝 테스트 데이터 생성: " + testDataCount + "건");
        createTestData(testDataCount);

        // 인덱스 생성
        System.out.println("📝 인덱스 생성");
        policyElasticSearchService.ensureIndex();
        waitForIndexCreation();

        // ES 인덱싱
        System.out.println("📝 Elasticsearch 인덱싱");
        policyElasticSearchService.reindexAllFromDb();
        waitForIndexing(testDataCount);

        System.out.println("✅ 준비 완료\n");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (!elasticsearchAvailable) {
            return;
        }

        // 모든 policy* 인덱스 정리
        try {
            var response = elasticsearchClient.cat().indices();
            for (var index : response.valueBody()) {
                String indexName = index.index();
                if (indexName != null && indexName.startsWith("policy")) {
                    try {
                        elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(indexName)));
                    } catch (Exception e) {
                        // 무시
                    }
                }
            }
            Thread.sleep(500);
        } catch (Exception e) {
            // 무시
        }
    }

    /**
     * 인덱스 생성 대기
     */
    private void waitForIndexCreation() throws Exception {
        for (int i = 0; i < 30; i++) {
            try {
                if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                    System.out.println("  - 인덱스 생성 확인");
                    Thread.sleep(500);
                    return;
                }
            } catch (Exception e) {
                // 계속 시도
            }
            Thread.sleep(200);
        }
        throw new AssertionError("❌ 인덱스 생성 실패");
    }

    /**
     * Elasticsearch 인덱싱 완료 대기
     */
    private void waitForIndexing(long expectedCount) throws Exception {
        System.out.println("  - 인덱싱 대기: " + expectedCount + "건");

        elasticsearchClient.indices().refresh(r -> r.index(INDEX));

        long lastCount = -1;
        for (int attempt = 0; attempt < MAX_WAIT_ATTEMPTS; attempt++) {
            try {
                long count = elasticsearchClient
                        .count(CountRequest.of(c -> c.index(INDEX)))
                        .count();

                if (count != lastCount && attempt % 10 == 0) {
                    System.out.println("    현재: " + count + " / " + expectedCount);
                    lastCount = count;
                }

                if (count >= expectedCount) {
                    System.out.println("  - 인덱싱 완료: " + count + "건");
                    Thread.sleep(1000); // 최종 안정화
                    return;
                }

                if (attempt > 0 && attempt % 10 == 0) {
                    elasticsearchClient.indices().refresh(r -> r.index(INDEX));
                }
            } catch (Exception e) {
                if (attempt % 10 == 0) {
                    System.out.println("    에러: " + e.getMessage());
                }
            }

            Thread.sleep(WAIT_INTERVAL_MS);
        }

        throw new AssertionError("❌ 인덱싱 타임아웃: " + expectedCount + "건 대기 실패");
    }

    @Test
    @DisplayName("나이 조건 검색 성능 비교")
    void comparePerformance_byAge() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // DB: 정책 나이 [min,max]가 사용자 구간 [25,35] 안에 포함(min≥25, max≤35). createTestData에서 i%10==0 인 정책이 [25,35].
        // ES: 사용자 나이 30이 정책 [min,max]에 포함(min≤30, max≥30).
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(25, 35, null, null, null, null, null);

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().age(30).build();

        // When & Then
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        printComparisonResult("나이 조건 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("소득 조건 검색 성능 비교")
    void comparePerformance_byEarn() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // DB: 정책 소득 [earnMin,earnMax]가 사용자 구간 [2000,4000] 안에 포함(earnMin≥2000, earnMax≤4000).
        //     → [2k,3k], [3k,4k]만 매칭(20건). ES: 사용자 소득 3000이 정책 구간에 포함 → 동일 20건.
        //     (기존 2000~5000이면 [4k,5k]까지 포함돼 DB 30건, ES 20건으로 어긋남)
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(null, null, null, null, null, 2000, 4000);

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().earn(3000).build();

        // When & Then
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        printComparisonResult("소득 조건 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("지역 코드 검색 성능 비교")
    void comparePerformance_byRegion() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(null, null, "11", null, null, null, null);

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().regionCode("11").build();

        // When & Then
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        printComparisonResult("지역 코드 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("키워드 검색 성능 비교 (ES만 지원)")
    void comparePerformance_byKeyword() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given
        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().keyword("청년").build();

        // When & Then
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("=".repeat(80));
        System.out.println("키워드 검색 (ES 전용 기능)");
        System.out.println("  결과 수: " + esResult.getResultCount());
        System.out.println("  평균 응답 시간: " + esResult.getAverageTime() + "ms");
        System.out.println("  중간값: " + esResult.getMedianTime() + "ms");
        System.out.println("  최소/최대: " + esResult.getMinTime() + "/" + esResult.getMaxTime() + "ms");
        System.out.println("=".repeat(80));
    }

    @Test
    @DisplayName("복합 조건 검색 성능 비교")
    void comparePerformance_byMultipleConditions() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(20, 39, "11", null, null, 0, 5000);

        PolicySearchCondition esCondition = PolicySearchCondition.builder()
                .age(25)
                .regionCode("11")
                .earn(3000)
                .build();

        // When & Then
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        printComparisonResult("복합 조건 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("전체 검색 성능 비교 (조건 없음)")
    void comparePerformance_all() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(null, null, null, null, null, null, null);

        PolicySearchCondition esCondition = PolicySearchCondition.builder().build();

        // When & Then
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        printComparisonResult("전체 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("데이터 양에 따른 성능 비교 (100 vs 1000 vs 10000)")
    void comparePerformance_byDataSize() {
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        System.out.println("=".repeat(80));
        System.out.println("데이터 양에 따른 성능 테스트");
        System.out.println("현재 데이터: " + testDataCount + "건");
        System.out.println("더 많은 데이터로 테스트하려면: -Dtest.data.count=1000");
        System.out.println("=".repeat(80));

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().age(30).build();

        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("ES 검색 성능 (" + testDataCount + "건)");
        System.out.println("  평균: " + esResult.getAverageTime() + "ms");
        System.out.println("  중간값: " + esResult.getMedianTime() + "ms");
    }

    // ========== Helper Methods ==========
    private void createTestData(int count) {
        List<Policy> policies = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            int minAge;
            int maxAge;
            if (i % 10 == 0) {
                // DB 나이 조건(25~35)에 매칭되도록: 정책 [25,35]
                minAge = 25;
                maxAge = 35;
            } else {
                minAge = 20 + (i % 50);
                maxAge = 40 + (i % 30);
            }

            Policy policy = Policy.builder()
                    .plcyNo("PERF-" + i + "-" + uniqueId)
                    .plcyNm("정책 " + i)
                    .sprtTrgtMinAge(String.valueOf(minAge))
                    .sprtTrgtMaxAge(String.valueOf(maxAge))
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("연소득")
                    .earnMinAmt(String.valueOf((i % 10) * 1000))
                    .earnMaxAmt(String.valueOf((i % 10 + 1) * 1000))
                    .zipCd(String.valueOf(11 + (i % 17)))
                    .jobCd("J" + String.format("%02d", i % 10))
                    .schoolCd("S" + String.format("%02d", i % 5))
                    .mrgSttsCd(i % 2 == 0 ? "Y" : "N")
                    .plcyKywdNm((i % 2 == 0 ? "청년" : "중장년") + ",지원")
                    .plcyExplnCn("정책 설명 " + i)
                    .build();

            policies.add(policy);
        }

        policyRepository.saveAll(policies);
        policyRepository.flush();
    }

    private PerformanceResult measureDbPerformance(Supplier<List<PolicySearchResponseDto>> supplier) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            supplier.get();
        }

        // Measure
        List<Long> times = new ArrayList<>();
        int resultCount = 0;

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long start = System.nanoTime();
            List<PolicySearchResponseDto> results = supplier.get();
            long end = System.nanoTime();

            times.add(TimeUnit.NANOSECONDS.toMillis(end - start));
            if (i == 0) {
                resultCount = results.size();
            }
        }

        return new PerformanceResult(times, resultCount);
    }

    private PerformanceResult measureEsPerformance(Supplier<?> supplier) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            supplier.get();
        }

        // Measure
        List<Long> times = new ArrayList<>();
        int resultCount = 0;

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long start = System.nanoTime();
            Object results = supplier.get();
            long end = System.nanoTime();

            times.add(TimeUnit.NANOSECONDS.toMillis(end - start));
            if (i == 0 && results instanceof List) {
                resultCount = ((List<?>) results).size();
            }
        }

        return new PerformanceResult(times, resultCount);
    }

    private void printComparisonResult(String testName, PerformanceResult dbResult, PerformanceResult esResult) {
        System.out.println("=".repeat(80));
        System.out.println(testName);
        System.out.println("-".repeat(80));
        System.out.println("DB 검색:");
        System.out.println("  결과 수: " + dbResult.getResultCount());
        System.out.println("  평균: " + dbResult.getAverageTime() + "ms");
        System.out.println("  중간값: " + dbResult.getMedianTime() + "ms");
        System.out.println("  최소/최대: " + dbResult.getMinTime() + "/" + dbResult.getMaxTime() + "ms");
        System.out.println();
        System.out.println("ES 검색:");
        System.out.println("  결과 수: " + esResult.getResultCount());
        System.out.println("  평균: " + esResult.getAverageTime() + "ms");
        System.out.println("  중간값: " + esResult.getMedianTime() + "ms");
        System.out.println("  최소/최대: " + esResult.getMinTime() + "/" + esResult.getMaxTime() + "ms");
        System.out.println();

        double improvement =
                ((double) (dbResult.getAverageTime() - esResult.getAverageTime()) / dbResult.getAverageTime()) * 100;
        System.out.println(
                "성능 차이: " + String.format("%.2f%%", improvement) + (improvement > 0 ? " (ES가 빠름)" : " (DB가 빠름)"));
        System.out.println("=".repeat(80));
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }

    private static class PerformanceResult {
        private final List<Long> times;
        private final int resultCount;

        public PerformanceResult(List<Long> times, int resultCount) {
            this.times = new ArrayList<>(times);
            this.times.sort(Long::compareTo);
            this.resultCount = resultCount;
        }

        public long getAverageTime() {
            return (long) times.stream().mapToLong(Long::longValue).average().orElse(0);
        }

        public long getMedianTime() {
            return times.get(times.size() / 2);
        }

        public long getMinTime() {
            return times.get(0);
        }

        public long getMaxTime() {
            return times.get(times.size() - 1);
        }

        public int getResultCount() {
            return resultCount;
        }
    }
}
