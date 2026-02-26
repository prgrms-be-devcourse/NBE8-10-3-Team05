package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.document.PolicyDocument
import com.back.domain.welfare.policy.search.PolicySearchCondition
import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

/**
 * PolicyElasticSearchService 사용 예시 (컨트롤러에서 어떻게 사용하는지)
 *
 * ⚠️ 이 파일은 예시용입니다. 실제 컨트롤러에 적용할 때는 이 패턴을 참고하세요.
 */
@RestController
@RequestMapping("/exampleApi/v1/welfare/policy/es/")
@RequiredArgsConstructor
class PolicyElasticSearchServiceUsageExample {
    private val policyElasticSearchService: PolicyElasticSearchService? = null

    /**
     * 예시 1: PolicySearchCondition을 사용한 고급 검색
     *
     * GET /api/v1/welfare/policy/es/search?keyword=청년&age=25&earn=3000&regionCode=11&from=0&size=10
     */
    @GetMapping("/search")
    @Throws(IOException::class)
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) age: Int?,
        @RequestParam(required = false) earn: Int?,
        @RequestParam(required = false) regionCode: String?,
        @RequestParam(required = false) jobCode: String?,
        @RequestParam(required = false) schoolCode: String?,
        @RequestParam(required = false) marriageStatus: String?,
        @RequestParam(required = false) keywords: MutableList<String?>?,
        @RequestParam(defaultValue = "0") from: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): List<PolicyDocument?> {
        // 1. PolicySearchCondition 객체 생성

        val condition = PolicySearchCondition(
            keyword = keyword,
            age = age,
            earn = earn,
            regionCode = regionCode,
            jobCode = jobCode,
            schoolCode = schoolCode,
            marriageStatus = marriageStatus,
            keywords = keywords
        )

        // 2. PolicyElasticSearchService의 search 메서드 호출
        return policyElasticSearchService!!.search(condition, from, size)
    }

    /**
     * 예시 2: 총 개수와 함께 검색 (페이징 정보 포함)
     *
     * GET /api/v1/welfare/policy/es/search-with-total?keyword=주거&age=30&from=0&size=20
     */
    @GetMapping("/search-with-total")
    @Throws(IOException::class)
    fun searchWithTotal(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) age: Int?,
        @RequestParam(required = false) earn: Int?,
        @RequestParam(required = false) regionCode: String?,
        @RequestParam(defaultValue = "0") from: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): PolicyElasticSearchService.SearchResult? {
        val condition = PolicySearchCondition(
            keyword = keyword,
            age = age,
            earn = earn,
            regionCode = regionCode
        )
        // 총 개수와 함께 반환
        return policyElasticSearchService!!.searchWithTotal(condition, from, size)
    }

    /**
     * 예시 3: 간단한 키워드 검색만 (기존 메서드 사용)
     *
     * GET /api/v1/welfare/policy/es/search-keyword?keyword=청년&from=0&size=10
     */
    @GetMapping("/search-keyword")
    @Throws(IOException::class)
    fun searchByKeyword(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") from: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): List<PolicyDocument?> {
        return policyElasticSearchService!!.searchByKeyword(keyword, from, size)
    }
}
