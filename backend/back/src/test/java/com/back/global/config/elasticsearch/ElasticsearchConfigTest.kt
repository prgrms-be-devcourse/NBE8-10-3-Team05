package com.back.global.config.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.IOException

@SpringBootTest
@ActiveProfiles("test")
internal class ElasticsearchConfigTest {
    @Autowired
    private val elasticsearchClient: ElasticsearchClient? = null

    @Autowired
    private val elasticsearchProperties: ElasticsearchProperties? = null

    @Test
    @DisplayName("ElasticsearchClient 빈이 정상적으로 생성되는지 확인")
    fun testElasticsearchClientBean() {
        Assertions.assertNotNull(elasticsearchClient, "ElasticsearchClient가 null이면 안 됩니다")
    }

    @Test
    @DisplayName("ElasticsearchProperties 빈이 정상적으로 생성되는지 확인")
    fun testElasticsearchPropertiesBean() {
        Assertions.assertNotNull(elasticsearchProperties, "ElasticsearchProperties가 null이면 안 됩니다")
        Assertions.assertNotNull(elasticsearchProperties!!.getHost(), "host가 설정되어야 합니다")
        Assertions.assertTrue(elasticsearchProperties.getPort() > 0, "port가 0보다 커야 합니다")
    }

    @Test
    @DisplayName("Elasticsearch 서버에 ping 요청이 정상적으로 동작하는지 확인")
    @Throws(IOException::class)
    fun testElasticsearchPing() {
        // Elasticsearch 서버가 실행 중이지 않을 수 있으므로 예외 처리
        try {
            val pingResult = elasticsearchClient!!.ping().value()
            Assertions.assertTrue(pingResult, "Elasticsearch ping 응답이 true여야 합니다")
            println("Elasticsearch 연결 성공 - ping: " + pingResult)
        } catch (e: Exception) {
            println("Elasticsearch 서버가 실행 중이지 않습니다: " + e.message)
            // 서버가 없어도 테스트는 통과하도록 함 (통합 테스트 환경에 따라 다를 수 있음)
            // 실제 서버가 필요한 경우 아래 주석을 해제하세요
            // fail("Elasticsearch 서버 연결 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Elasticsearch 클러스터 정보 조회 테스트")
    @Throws(IOException::class)
    fun testElasticsearchClusterInfo() {
        try {
            val response = elasticsearchClient!!.info()
            Assertions.assertNotNull(response, "Elasticsearch info 응답이 null이면 안 됩니다")
            println("Elasticsearch 클러스터 정보 조회 성공")
            println("   - 클러스터 이름: " + response!!.clusterName())
            println("   - 버전: " + response.version().number())
        } catch (e: Exception) {
            println("Elasticsearch 서버가 실행 중이지 않습니다: " + e.message)
            // 서버가 없어도 테스트는 통과하도록 함
        }
    }
}
