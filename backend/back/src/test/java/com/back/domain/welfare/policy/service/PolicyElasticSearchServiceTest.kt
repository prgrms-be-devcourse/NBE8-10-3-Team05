package com.back.domain.welfare.policy.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
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

    private var sut: PolicyElasticSearchService? = null

    @Mock
    private val booleanResponse: co.elastic.clients.transport.endpoints.BooleanResponse? = null

    @BeforeEach
    fun setUp() {
        sut = PolicyElasticSearchService(
            esClient!!,
            policyRepository!!,
            policyDocumentMapper!!,
            policyQueryBuilder!!,
            "policy"
        )
        Mockito.lenient().`when`(esClient!!.indices()).thenReturn(indicesClient)

        // exists() 가 null 반환하지 않도록 stub 추가
        Mockito.lenient().`when`(
            indicesClient!!.exists(anyNonNull<co.elastic.clients.elasticsearch.indices.ExistsRequest>())
        ).thenReturn(booleanResponse)

        // 인덱스가 존재한다고 설정
        Mockito.lenient().`when`(booleanResponse!!.value()).thenReturn(true)
    }

    @Nested
    @DisplayName("search(condition, from, size)")
    internal inner class Search {
        @Test
        @DisplayName("PolicyQueryBuilder.build 호출 후, search 결과 문서 반환")
        @Throws(IOException::class)
        fun usesQueryBuilder_returnsDocuments() {
            val condition = PolicySearchCondition(keyword = "주거")
            val query =
                Query.of(Function { q: Query.Builder? -> q!!.matchAll(Function { m: MatchAllQuery.Builder? -> m }) })
            Mockito.`when`(policyQueryBuilder!!.build(condition)).thenReturn(query)

            val doc = PolicyDocument(policyId = 2, plcyNm = "주거")
            val mockResp = buildSearchResponse(mutableListOf(doc), 1L)
            Mockito.doReturn(mockResp).`when`(esClient)?.search(
                anyNonNull<Function<co.elastic.clients.elasticsearch.core.SearchRequest.Builder, ObjectBuilder<co.elastic.clients.elasticsearch.core.SearchRequest>>>(),
                anyNonNull<Class<PolicyDocument>>()
            )

            val result = sut!!.search(condition, 0, 10)

            Assertions.assertThat(result).hasSize(1)
            Assertions.assertThat(result!![0]!!.plcyNm).isEqualTo("주거")
            Mockito.verify(policyQueryBuilder).build(condition)
            Mockito.verify(esClient)?.search(
                anyNonNull<Function<co.elastic.clients.elasticsearch.core.SearchRequest.Builder, ObjectBuilder<co.elastic.clients.elasticsearch.core.SearchRequest>>>(),
                anyNonNull<Class<PolicyDocument>>()
            )
        }
    }

    @Nested
    @DisplayName("searchWithTotal")
    internal inner class SearchWithTotal {
        @Test
        @DisplayName("문서 목록과 total 반환")
        @Throws(IOException::class)
        fun returnsDocumentsAndTotal() {
            val condition = PolicySearchCondition(keyword = "청년")
            val query =
                Query.of(Function { q: Query.Builder? -> q!!.matchAll(Function { m: MatchAllQuery.Builder? -> m }) })
            Mockito.`when`(policyQueryBuilder!!.build(condition)).thenReturn(query)

            val doc = PolicyDocument(policyId = 1, plcyNm = "청년")
            val mockResp = buildSearchResponse(mutableListOf(doc), 100L)
            Mockito.doReturn(mockResp).`when`(esClient)?.search(
                anyNonNull<Function<co.elastic.clients.elasticsearch.core.SearchRequest.Builder, ObjectBuilder<co.elastic.clients.elasticsearch.core.SearchRequest>>>(),
                anyNonNull<Class<PolicyDocument>>()
            )

            val result = sut!!.searchWithTotal(condition, 0, 10)

            Assertions.assertThat(result.documents).hasSize(1)
            Assertions.assertThat(result.total).isEqualTo(100L)
            Mockito.verify(policyQueryBuilder).build(condition)
            Mockito.verify(esClient)?.search(
                anyNonNull<Function<co.elastic.clients.elasticsearch.core.SearchRequest.Builder, ObjectBuilder<co.elastic.clients.elasticsearch.core.SearchRequest>>>(),
                anyNonNull<Class<PolicyDocument>>()
            )
        }
    }

    @Nested
    @DisplayName("SearchResult")
    internal inner class SearchResultTest {
        @Test
        @DisplayName("documents / total 동작")
        fun getters() {
            val doc = PolicyDocument(policyId = 1, plcyNm = "a")
            val result = PolicyElasticSearchService.SearchResult(listOf(doc), 50L)

            Assertions.assertThat(result.documents).containsExactly(doc)
            Assertions.assertThat(result.total).isEqualTo(50L)
        }
    }

    companion object {
        /**
         * Kotlin + Mockito 에서 non-nullable 타입 파라미터가 필요한 메서드에
         * any() 매처를 전달할 때 발생하는 NPE/타입 불일치를 해결하는 helper.
         * (mockito-kotlin 라이브러리의 any() 구현 방식과 동일)
         */
        @Suppress("UNCHECKED_CAST")
        private fun <T> anyNonNull(): T = Mockito.any<T>() ?: null as T

        @Suppress("UNCHECKED_CAST")
        private fun buildSearchResponse(
            documents: MutableList<PolicyDocument?>,
            total: Long
        ): SearchResponse<PolicyDocument> {
            val hits = documents.map { d ->
                val h = Mockito.mock(Hit::class.java) as Hit<PolicyDocument>
                Mockito.`when`(h.source()).thenReturn(d)
                h
            }.toMutableList()

            val totalHits = Mockito.mock(TotalHits::class.java)
            Mockito.`when`(totalHits.value()).thenReturn(total)

            val meta = Mockito.mock(HitsMetadata::class.java) as HitsMetadata<PolicyDocument>
            Mockito.`when`(meta.hits()).thenReturn(hits)
            Mockito.`when`(meta.total()).thenReturn(totalHits)

            val resp = Mockito.mock(SearchResponse::class.java) as SearchResponse<PolicyDocument>
            Mockito.`when`(resp.hits()).thenReturn(meta)

            return resp
        }
    }
}
