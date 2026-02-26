package com.back.global.springBatch.elasticSearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.function.Function

@Component
class PolicyEsItemWriter(
    private val esClient: ElasticsearchClient,
    private val policyDocumentMapper: PolicyDocumentMapper
) : ItemWriter<Policy> {

    override fun write(chunk: Chunk<out Policy>) {
        if (chunk.isEmpty()) return

        val ops = mutableListOf<BulkOperation>()

        for (policy in chunk) {
            log.debug("🔍 처리 중인 Policy: bizId={}, title={}", policy.id, policy.plcyNm)

            val policyId = policy.id
                ?: throw RuntimeException("❌ Policy ID가 null입니다.")

            val doc = policyDocumentMapper.toDocument(policy)

            if (doc?.policyId == null) {
                log.error("❌ 매핑 실패: Policy -> PolicyDocument 변환 결과가 null입니다.")
                continue
            }

            ops += BulkOperation.of { b ->
                b.index { i ->
                    i.index(INDEX)
                        .id(doc.policyId.toString())
                        .document(doc)
                }
            }
        }

        if (ops.isEmpty()) {
            log.warn("⚠️ 전송할 데이터가 없습니다 (ops is empty).")
            return
        }

        try {
            val resp = esClient.bulk { b ->
                b.operations(ops)
            }

            if (resp.errors()) {
                log.warn(
                    "Elasticsearch bulk completed with errors. took={}, items={}",
                    resp.took(),
                    resp.items().size
                )
            } else {
                log.debug(
                    "Elasticsearch bulk completed. took={}, items={}",
                    resp.took(),
                    resp.items().size
                )
            }

        } catch (e: IOException) {
            log.error("ES 통신 중 오류 발생...", e)
            throw RuntimeException(e)
        }

        log.debug("Elasticsearch에 {}개의 데이터 동기화 완료...", chunk.size())
    }

    companion object {
        private val log: Logger =
            LoggerFactory.getLogger(PolicyEsItemWriter::class.java)

        private const val INDEX = "policy"
    }
}
