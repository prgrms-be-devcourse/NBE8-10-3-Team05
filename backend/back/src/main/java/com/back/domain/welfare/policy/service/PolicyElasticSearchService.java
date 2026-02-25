package com.back.domain.welfare.policy.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.search.PolicyQueryBuilder;
import com.back.domain.welfare.policy.search.PolicySearchCondition;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyElasticSearchService {
    private final ElasticsearchClient esClient;
    private final PolicyRepository policyRepository;
    private final PolicyDocumentMapper policyDocumentMapper;
    private final PolicyQueryBuilder policyQueryBuilder;

    private static final String DEFAULT_INDEX = "policy";

    @Value("${app.elasticsearch.policy-index:" + DEFAULT_INDEX + "}")
    private String indexName;

    /**
     * ES 인덱스가 없으면 생성합니다.
     * - 매핑은 "지금 필요한 최소"만 잡아두고, 추후 검색 고도화 시 확장하는 것을 권장합니다.
     */
    public void ensureIndex() throws IOException {
        boolean exists =
                esClient.indices().exists(ExistsRequest.of(r -> r.index(indexName))).value();

        if (exists) {
            return;
        }

        esClient.indices().create(c -> c.index(indexName).mappings(m -> m.properties("policyId", p -> p.integer(i -> i))
                .properties("plcyNo", p -> p.keyword(k -> k))
                .properties("plcyNm", p -> p.text(t -> t))
                .properties("minAge", p -> p.integer(i -> i))
                .properties("maxAge", p -> p.integer(i -> i))
                .properties("ageLimited", p -> p.boolean_(b -> b))
                .properties("earnCondition", p -> p.keyword(k -> k))
                .properties("earnMin", p -> p.integer(i -> i))
                .properties("earnMax", p -> p.integer(i -> i))
                .properties("regionCode", p -> p.keyword(k -> k))
                .properties("jobCode", p -> p.keyword(k -> k))
                .properties("schoolCode", p -> p.keyword(k -> k))
                .properties("marriageStatus", p -> p.keyword(k -> k))
                .properties("keywords", p -> p.keyword(k -> k))
                .properties("specialBizCode", p -> p.keyword(k -> k))
                .properties("description", p -> p.text(t -> t))));

        log.info("Elasticsearch index created: {}", indexName);
    }

    /**
     * DB의 Policy 전체를 ES에 Bulk로 다시 적재합니다.
     * - 운영에서는 스케줄러/배치/관리자 API에서 호출하는 형태를 권장합니다.
     */
    @Transactional
    public long reindexAllFromDb() throws IOException {
        ensureIndex();

        List<Policy> policies = policyRepository.findAll();
        if (policies.isEmpty()) {
            return 0;
        }

        List<BulkOperation> ops = new ArrayList<>(policies.size());
        for (Policy policy : policies) {
            PolicyDocument doc = policyDocumentMapper.toDocument(policy);
            if (doc == null || doc.getPolicyId() == null) {
                continue;
            }

            ops.add(BulkOperation.of(b -> b.index(
                    i -> i.index(indexName).id(String.valueOf(doc.getPolicyId())).document(doc))));
        }

        var resp = esClient.bulk(b -> b.operations(ops).refresh(Refresh.True));
        if (resp.errors()) {
            // 에러 상세는 item별로 존재하므로, 우선 전체 에러만 로그로 남김 (필요 시 확장)
            log.warn(
                    "Elasticsearch bulk reindex completed with errors. took={}, items={}",
                    resp.took(),
                    resp.items().size());
        } else {
            log.info(
                    "Elasticsearch bulk reindex completed. took={}, items={}",
                    resp.took(),
                    resp.items().size());
        }

        return ops.size();
    }

    /**
     * 키워드 검색(기본): 정책명/설명/키워드 배열을 대상으로 검색합니다.
     * - 컨트롤러/요구사항에 맞춰 필드/가중치/정렬을 쉽게 확장할 수 있게 구성했습니다.
     */
    public List<PolicyDocument> searchByKeyword(String keyword, int from, int size) throws IOException {
        String q = (keyword == null) ? "" : keyword.trim();

        SearchResponse<PolicyDocument> response = esClient.search(
                s -> s.index(indexName)
                        .from(Math.max(from, 0))
                        .size(Math.min(Math.max(size, 1), 100))
                        .query(query -> query.bool(b -> {
                            if (q.isEmpty()) {
                                // 키워드가 없으면 match_all
                                return b.must(m -> m.matchAll(ma -> ma));
                            }
                            return b.must(m -> m.multiMatch(mm ->
                                    mm.query(q).operator(Operator.And).fields("plcyNm^3", "description", "keywords")));
                        })),
                PolicyDocument.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * PolicySearchCondition을 사용한 고급 검색
     * - PolicyQueryBuilder를 통해 복합 조건(나이, 소득, 지역, 직업, 학력, 결혼상태, 키워드)을 지원합니다.
     *
     * @param condition 검색 조건 (keyword, age, earn, regionCode, jobCode, schoolCode, marriageStatus, keywords)
     * @param from 페이지네이션 시작 위치 (0부터 시작)
     * @param size 한 번에 가져올 문서 개수 (1~100)
     * @return 검색된 PolicyDocument 리스트
     * @throws IOException Elasticsearch 연결/쿼리 오류 시
     */
    public List<PolicyDocument> search(PolicySearchCondition condition, int from, int size) throws IOException {
        // 인덱스 없으면 빈 리스트 반환
        boolean exists = esClient.indices()
            .exists(ExistsRequest.of(r -> r.index(indexName)))
            .value();
        if (!exists) {
            log.warn("Elasticsearch index '{}' does not exist. Returning empty result.", indexName);
            return List.of();
        }

        Query query = policyQueryBuilder.build(condition);

        SearchResponse<PolicyDocument> response = esClient.search(
            s -> s.index(indexName)
                .from(Math.max(from, 0))
                .size(Math.min(Math.max(size, 1), 100))
                .query(query),
            PolicyDocument.class);

        return response.hits().hits().stream()
            .map(hit -> hit.source())
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * PolicySearchCondition을 사용한 고급 검색 (총 개수 포함)
     * - 페이징 정보와 함께 전체 검색 결과 개수를 반환합니다.
     *
     * @param condition 검색 조건
     * @param from 페이지네이션 시작 위치
     * @param size 한 번에 가져올 문서 개수
     * @return 검색 결과와 총 개수를 포함한 SearchResult 객체
     * @throws IOException Elasticsearch 연결/쿼리 오류 시
     */
    public SearchResult searchWithTotal(PolicySearchCondition condition, int from, int size) throws IOException {
        boolean exists =
                esClient.indices().exists(ExistsRequest.of(r -> r.index(indexName))).value();
        if (!exists) {
            log.warn(
                    "Elasticsearch index '{}' does not exist. Returning empty result with total=0.",
                    indexName);
            return new SearchResult(List.of(), 0L);
        }

        Query query = policyQueryBuilder.build(condition);

        SearchResponse<PolicyDocument> response = esClient.search(
                s -> s.index(indexName)
                        .from(Math.max(from, 0))
                        .size(Math.min(Math.max(size, 1), 100))
                        .query(query),
                PolicyDocument.class);

        List<PolicyDocument> documents = response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .toList();

        long total =
                (response.hits().total() != null) ? response.hits().total().value() : 0L;

        return new SearchResult(documents, total);
    }

    /**
     * 검색 결과와 총 개수를 담는 내부 클래스
     */
    public static class SearchResult {
        private final List<PolicyDocument> documents;
        private final long total;

        public SearchResult(List<PolicyDocument> documents, long total) {
            this.documents = documents;
            this.total = total;
        }

        public List<PolicyDocument> getDocuments() {
            return documents;
        }

        public long getTotal() {
            return total;
        }
    }
}
