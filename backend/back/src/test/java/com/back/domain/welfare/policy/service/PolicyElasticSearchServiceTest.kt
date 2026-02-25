package com.back.domain.welfare.policy.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.elasticsearch.core.search.HitsMetadata
import co.elastic.clients.elasticsearch.core.search.TotalHits
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import co.elastic.clients.util.ObjectBuilder
import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.domain.welfare.policy.search.PolicyQueryBuilder
import com.back.domain.welfare.policy.search.PolicySearchCondition
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.io.IOException
import java.util.function.Function

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PolicyElasticSearchService 단위 테스트")
internal class PolicyElasticSearchServiceTest {

    @Mock
    private val esClient: ElasticsearchClient? = null

    @Mock
    private val policyRepository: PolicyRepository? = null

    @Mock
    private val policyDocumentMapper: PolicyDocumentMapper? = null

    @Mock
    private val policyQueryBuilder: PolicyQueryBuilder? = null

    @Mock
    private val indicesClient: ElasticsearchIndicesClient? = null

    @InjectMocks
    private val sut: PolicyElasticSearchService? = null

    @BeforeEach
    fun setUp() {
        Mockito.lenient().`when`(esClient!!.indices()).thenReturn(indicesClient)
    }

    @Nested
    @DisplayName("search(condition, from, size)")
    internal inner class Search {

        @Test
        @DisplayName("PolicyQueryBuilder.build 호출 후, search 결과 문서 반환")
        @Throws(IOException::class)
        @Suppress("UNCHECKED_CAST")
        fun usesQueryBuilder_returnsDocuments() {
            // val이므로 생성자 named parameter로 전달
            val condition = PolicySearchCondition(keyword = "주거")

            val query = Query.of(Function { q: Query.Builder? ->
                q!!.matchAll(Function { m: MatchAllQuery.Builder? -> m })
            })
            Mockito.`when`(policyQueryBuilder!!.build(condition)).thenReturn(query)

            val doc = PolicyDocument().apply { plcyNm = "주거" }
            val mockResp: SearchResponse<PolicyDocument?> = buildSearchResponse(mutableListOf(doc), 1L)

            // search() 시그니처: (Function<Builder, ObjectBuilder<SearchRequest>>, Class<T>) → 명시적 캐스트
            Mockito.doReturn(mockResp).`when`(esClient!!).search(
                ArgumentMatchers.any() as Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>?,
                ArgumentMatchers.any() as Class<PolicyDocument?>?
            )

            val result = sut!!.search(condition, 0, 10).toMutableList()

            Assertions.assertThat(result).hasSize(1)
            Assertions.assertThat(result[0]!!.plcyNm).isEqualTo("주거")
            Mockito.verify(policyQueryBuilder).build(condition)
            Mockito.verify(esClient).search(
                ArgumentMatchers.any() as Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>?,
                ArgumentMatchers.any() as Class<PolicyDocument?>?
            )
        }
    }

    @Nested
    @DisplayName("searchWithTotal")
    internal inner class SearchWithTotal {

        @Test
        @DisplayName("문서 목록과 total 반환")
        @Throws(IOException::class)
        @Suppress("UNCHECKED_CAST")
        fun returnsDocumentsAndTotal() {
            val condition = PolicySearchCondition(keyword = "청년")

            val query = Query.of(Function { q: Query.Builder? ->
                q!!.matchAll(Function { m: MatchAllQuery.Builder? -> m })
            })
            Mockito.`when`(policyQueryBuilder!!.build(condition)).thenReturn(query)

            val doc = PolicyDocument().apply { plcyNm = "청년" }
            val mockResp: SearchResponse<PolicyDocument?> = buildSearchResponse(mutableListOf(doc), 100L)

            Mockito.doReturn(mockResp).`when`(esClient!!).search(
                ArgumentMatchers.any() as Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>?,
                ArgumentMatchers.any() as Class<PolicyDocument?>?
            )

            val result = sut!!.searchWithTotal(condition, 0, 10)

            Assertions.assertThat(result.documents).hasSize(1)
            Assertions.assertThat(result.total).isEqualTo(100L)
            Mockito.verify(policyQueryBuilder).build(condition)
            Mockito.verify(esClient).search(
                ArgumentMatchers.any() as Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>?,
                ArgumentMatchers.any() as Class<PolicyDocument?>?
            )
        }
    }

    @Nested
    @DisplayName("SearchResult")
    internal inner class SearchResultTest {

        @Test
        @DisplayName("getDocuments / getTotal 동작")
        fun getters() {
            val doc = PolicyDocument().apply { plcyNm = "a" }
            val result = PolicyElasticSearchService.SearchResult(listOf(doc), 50L)

            Assertions.assertThat(result.documents).containsExactly(doc)
            Assertions.assertThat(result.total).isEqualTo(50L)
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        private fun buildSearchResponse(
            documents: MutableList<PolicyDocument?>,
            total: Long
        ): SearchResponse<PolicyDocument?> {
            val hits = documents.map { d ->
                val h = Mockito.mock(Hit::class.java) as Hit<PolicyDocument?>
                Mockito.`when`(h.source()).thenReturn(d)
                h
            }.toMutableList()

            val totalHits = Mockito.mock(TotalHits::class.java)
            Mockito.`when`(totalHits.value()).thenReturn(total)

            val meta = Mockito.mock(HitsMetadata::class.java) as HitsMetadata<PolicyDocument?>
            Mockito.`when`(meta.hits()).thenReturn(hits)
            Mockito.`when`(meta.total()).thenReturn(totalHits)

            val resp = Mockito.mock(SearchResponse::class.java) as SearchResponse<PolicyDocument?>
            Mockito.`when`(resp.hits()).thenReturn(meta)

            return resp
        }
    }
}
