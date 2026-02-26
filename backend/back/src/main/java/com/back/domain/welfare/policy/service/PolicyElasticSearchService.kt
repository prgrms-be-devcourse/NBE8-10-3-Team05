package com.back.domain.welfare.policy.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.mapping.*
import co.elastic.clients.elasticsearch._types.query_dsl.*
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation
import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.domain.welfare.policy.search.PolicyQueryBuilder
import com.back.domain.welfare.policy.search.PolicySearchCondition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.*
import java.util.function.Function
import kotlin.math.max
import kotlin.math.min

@Service
@Transactional(readOnly = true)
class PolicyElasticSearchService(                              // ✅ 생성자 주입
    private val esClient: ElasticsearchClient,
    private val policyRepository: PolicyRepository,
    private val policyDocumentMapper: PolicyDocumentMapper,
    private val policyQueryBuilder: PolicyQueryBuilder,
    @Value("\${app.elasticsearch.policy-index:policy}") private val indexName: String
) {
    companion object {
        private val log = LoggerFactory.getLogger(PolicyElasticSearchService::class.java) // ✅ @Slf4j 대체
    }

    @Throws(IOException::class)
    fun ensureIndex() {
        val exists = esClient.indices()
            .exists(ExistsRequest.of { r -> r.index(indexName) })
            .value()

        if (exists) return

        esClient.indices().create { c ->
            c.index(indexName).mappings { m ->
                m.properties("policyId") { p -> p.integer { i -> i } }
                    .properties("plcyNo") { p -> p.keyword { k -> k } }
                    .properties("plcyNm") { p -> p.text { t -> t } }
                    .properties("minAge") { p -> p.integer { i -> i } }
                    .properties("maxAge") { p -> p.integer { i -> i } }
                    .properties("ageLimited") { p -> p.boolean_ { b -> b } }
                    .properties("earnCondition") { p -> p.keyword { k -> k } }
                    .properties("earnMin") { p -> p.integer { i -> i } }
                    .properties("earnMax") { p -> p.integer { i -> i } }
                    .properties("regionCode") { p -> p.keyword { k -> k } }
                    .properties("jobCode") { p -> p.keyword { k -> k } }
                    .properties("schoolCode") { p -> p.keyword { k -> k } }
                    .properties("marriageStatus") { p -> p.keyword { k -> k } }
                    .properties("keywords") { p -> p.keyword { k -> k } }
                    .properties("specialBizCode") { p -> p.keyword { k -> k } }
                    .properties("description") { p -> p.text { t -> t } }
            }
        }

        log.info("Elasticsearch index created: {}", indexName)
    }

    @Transactional
    @Throws(IOException::class)
    fun reindexAllFromDb(): Long {
        ensureIndex()

        val policies = policyRepository.findAll()
        if (policies.isEmpty()) return 0

        val ops = mutableListOf<BulkOperation>()
        for (policy in policies) {
            val doc = policyDocumentMapper.toDocument(policy) ?: continue
            if (doc.policyId == null) continue                 // ✅ private 필드는 PolicyDocument에서 공개 필요

            ops.add(BulkOperation.of { b ->
                b.index<Any> { i ->
                    i.index(indexName).id(doc.policyId.toString()).document(doc)
                }
            })
        }

        val resp = esClient.bulk { b -> b.operations(ops).refresh(Refresh.True) }
        if (resp.errors()) {
            log.warn("Bulk reindex completed with errors. took={}, items={}", resp.took(), resp.items().size)
        } else {
            log.info("Bulk reindex completed. took={}, items={}", resp.took(), resp.items().size)
        }

        return ops.size.toLong()
    }

    @Throws(IOException::class)
    fun searchByKeyword(keyword: String?, from: Int, size: Int): List<PolicyDocument?> {
        val q = keyword?.trim() ?: ""

        val response = esClient.search<PolicyDocument>({ s ->
            s.index(indexName)
                .from(max(from, 0))
                .size(min(max(size, 1), 100))
                .query { query ->
                    query.bool { b ->
                        if (q.isEmpty()) {
                            b.must { m -> m.matchAll { ma -> ma } }
                        } else {
                            b.must { m ->
                                m.multiMatch { mm ->
                                    mm.query(q).operator(Operator.And).fields("plcyNm^3", "description", "keywords")
                                }
                            }
                        }
                    }
                }
        }, PolicyDocument::class.java)

        return response.hits().hits()
            .mapNotNull { hit -> hit.source() }
    }

    @Throws(IOException::class)
    fun search(condition: PolicySearchCondition?, from: Int, size: Int): List<PolicyDocument?> {
        if (!esClient.indices().exists(ExistsRequest.of { r -> r.index(indexName) }).value()) {
            log.warn("Elasticsearch index '{}' does not exist. Returning empty result.", indexName)
            return emptyList()
        }
        val query = policyQueryBuilder.build(condition)

        val response = esClient.search<PolicyDocument>({ s ->
            s.index(indexName)
                .from(max(from, 0))
                .size(min(max(size, 1), 100))
                .query(query)
        }, PolicyDocument::class.java)

        return response.hits().hits().mapNotNull { it.source() }
    }

    @Throws(IOException::class)
    fun searchWithTotal(condition: PolicySearchCondition?, from: Int, size: Int): SearchResult {
        if (!esClient.indices().exists(ExistsRequest.of { r -> r.index(indexName) }).value()) {
            log.warn("Elasticsearch index '{}' does not exist. Returning empty result with total=0.", indexName)
            return SearchResult(emptyList(), 0L)
        }
        val query = policyQueryBuilder.build(condition)

        val response = esClient.search<PolicyDocument>({ s ->
            s.index(indexName)
                .from(max(from, 0))
                .size(min(max(size, 1), 100))
                .query(query)
        }, PolicyDocument::class.java)

        val documents = response.hits().hits().mapNotNull { it.source() }
        val total = response.hits().total()?.value() ?: 0L
        return SearchResult(documents, total)
    }

    class SearchResult(val documents: List<PolicyDocument?>, val total: Long)
}
