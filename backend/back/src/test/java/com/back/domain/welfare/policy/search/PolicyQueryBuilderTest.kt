package com.back.domain.welfare.policy.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

@DisplayName("PolicyQueryBuilder 단위 테스트")
class PolicyQueryBuilderTest {

    private PolicyQueryBuilder policyQueryBuilder;

    @BeforeEach
    void setUp() {
        policyQueryBuilder = new PolicyQueryBuilder();
    }

    @Nested
    @DisplayName("build(PolicySearchCondition)")
    class Build {

        @Test
        @DisplayName("조건 없음(전부 null) → Query 생성, 예외 없음")
        void emptyCondition() {
            PolicySearchCondition condition = PolicySearchCondition.builder().build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }

        @Test
        @DisplayName("키워드만 있으면 → plcyNm match 포함 Query")
        void keywordOnly() {
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("청년 주거").build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }

        @Test
        @DisplayName("나이만 있으면 → minAge/maxAge range filter 포함")
        void ageOnly() {
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().age(25).build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }

        @Test
        @DisplayName("소득만 있으면 → earnMin/earnMax range filter 포함")
        void earnOnly() {
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().earn(3000).build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }

        @Test
        @DisplayName("지역/직업/학력/결혼상태 term filter")
        void termFilters() {
            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .regionCode("11")
                    .jobCode("J01")
                    .schoolCode("S01")
                    .marriageStatus("Y")
                    .build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }

        @Test
        @DisplayName("keywords 리스트 있으면 → terms should 포함")
        void keywordsList() {
            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .keywords(List.of("청년", "주거"))
                    .build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }

        @Test
        @DisplayName("모든 조건 조합 → 복합 bool Query")
        void allConditions() {
            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .keyword("청년")
                    .age(28)
                    .earn(2500)
                    .regionCode("11")
                    .jobCode("J01")
                    .schoolCode("S02")
                    .marriageStatus("N")
                    .keywords(List.of("취업"))
                    .build();

            Query query = policyQueryBuilder.build(condition);

            assertThat(query).isNotNull();
            assertThat(query.isBool()).isTrue();
        }
    }
}
