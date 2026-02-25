package com.back.domain.welfare.policy.search

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("PolicyQueryBuilder 단위 테스트")
class PolicyQueryBuilderTest {

    private lateinit var policyQueryBuilder: PolicyQueryBuilder

    @BeforeEach
    fun setUp() {
        policyQueryBuilder = PolicyQueryBuilder()
    }

    @Nested
    @DisplayName("build(PolicySearchCondition)")
    inner class Build {

        @Test
        @DisplayName("조건 없음(전부 null) → Query 생성, 예외 없음")
        fun emptyCondition() {
            val condition = PolicySearchCondition()

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }

        @Test
        @DisplayName("키워드만 있으면 → plcyNm match 포함 Query")
        fun keywordOnly() {
            val condition = PolicySearchCondition(keyword = "청년 주거")

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }

        @Test
        @DisplayName("나이만 있으면 → minAge/maxAge range filter 포함")
        fun ageOnly() {
            val condition = PolicySearchCondition(age = 25)

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }

        @Test
        @DisplayName("소득만 있으면 → earnMin/earnMax range filter 포함")
        fun earnOnly() {
            val condition = PolicySearchCondition(earn = 3000)

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }

        @Test
        @DisplayName("지역/직업/학력/결혼상태 term filter")
        fun termFilters() {
            val condition = PolicySearchCondition(
                regionCode = "11",
                jobCode = "J01",
                schoolCode = "S01",
                marriageStatus = "Y"
            )

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }

        @Test
        @DisplayName("keywords 리스트 있으면 → terms should 포함")
        fun keywordsList() {
            val condition = PolicySearchCondition(keywords = listOf("청년", "주거"))

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }

        @Test
        @DisplayName("모든 조건 조합 → 복합 bool Query")
        fun allConditions() {
            val condition = PolicySearchCondition(
                keyword = "청년",
                age = 28,
                earn = 2500,
                regionCode = "11",
                jobCode = "J01",
                schoolCode = "S02",
                marriageStatus = "N",
                keywords = listOf("취업")
            )

            val query: Query = policyQueryBuilder.build(condition)

            assertThat(query).isNotNull()
            assertThat(query.isBool).isTrue()
        }
    }
}
