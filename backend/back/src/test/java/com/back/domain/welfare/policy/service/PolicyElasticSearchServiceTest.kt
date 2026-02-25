package com.back.domain.welfare.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.search.PolicyQueryBuilder;
import com.back.domain.welfare.policy.search.PolicySearchCondition;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PolicyElasticSearchService 단위 테스트")
class PolicyElasticSearchServiceTest {

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyDocumentMapper policyDocumentMapper;

    @Mock
    private PolicyQueryBuilder policyQueryBuilder;

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @InjectMocks
    private PolicyElasticSearchService sut;

    @BeforeEach
    void setUp() {
        lenient().when(esClient.indices()).thenReturn(indicesClient);
    }

    @Nested
    @DisplayName("search(condition, from, size)")
    class Search {

        @Test
        @DisplayName("PolicyQueryBuilder.build 호출 후, search 결과 문서 반환")
        void usesQueryBuilder_returnsDocuments() throws IOException {
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("주거").build();
            Query query = Query.of(q -> q.matchAll(m -> m));
            when(policyQueryBuilder.build(condition)).thenReturn(query);

            PolicyDocument doc =
                    PolicyDocument.builder().policyId(2).plcyNm("주거").build();
            SearchResponse<PolicyDocument> mockResp = buildSearchResponse(List.of(doc), 1L);
            doReturn(mockResp).when(esClient).search(any(Function.class), eq(PolicyDocument.class));

            List<PolicyDocument> result = sut.search(condition, 0, 10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPlcyNm()).isEqualTo("주거");
            verify(policyQueryBuilder).build(condition);
            verify(esClient).search(any(Function.class), eq(PolicyDocument.class));
        }
    }

    @Nested
    @DisplayName("searchWithTotal")
    class SearchWithTotal {

        @Test
        @DisplayName("문서 목록과 total 반환")
        void returnsDocumentsAndTotal() throws IOException {
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("청년").build();
            Query query = Query.of(q -> q.matchAll(m -> m));
            when(policyQueryBuilder.build(condition)).thenReturn(query);

            PolicyDocument doc =
                    PolicyDocument.builder().policyId(1).plcyNm("청년").build();
            SearchResponse<PolicyDocument> mockResp = buildSearchResponse(List.of(doc), 100L);
            doReturn(mockResp).when(esClient).search(any(Function.class), eq(PolicyDocument.class));

            PolicyElasticSearchService.SearchResult result = sut.searchWithTotal(condition, 0, 10);

            assertThat(result.getDocuments()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(100L);
            verify(policyQueryBuilder).build(condition);
            verify(esClient).search(any(Function.class), eq(PolicyDocument.class));
        }
    }

    @Nested
    @DisplayName("SearchResult")
    class SearchResultTest {

        @Test
        @DisplayName("getDocuments / getTotal 동작")
        void getters() {
            PolicyDocument doc =
                    PolicyDocument.builder().policyId(1).plcyNm("a").build();
            var result = new PolicyElasticSearchService.SearchResult(List.of(doc), 50L);

            assertThat(result.getDocuments()).containsExactly(doc);
            assertThat(result.getTotal()).isEqualTo(50L);
        }
    }

    @SuppressWarnings("unchecked")
    private static SearchResponse<PolicyDocument> buildSearchResponse(List<PolicyDocument> documents, long total) {
        List<Hit<PolicyDocument>> hits = documents.stream()
                .map(d -> {
                    Hit<PolicyDocument> h = mock(Hit.class);
                    when(h.source()).thenReturn(d);
                    return h;
                })
                .toList();
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(total);
        HitsMetadata<PolicyDocument> meta = mock(HitsMetadata.class);
        when(meta.hits()).thenReturn(hits);
        when(meta.total()).thenReturn(totalHits);
        SearchResponse<PolicyDocument> resp = mock(SearchResponse.class);
        when(resp.hits()).thenReturn(meta);
        return resp;
    }
}
