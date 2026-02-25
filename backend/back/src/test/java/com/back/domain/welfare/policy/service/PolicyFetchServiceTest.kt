package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.Pagging
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PolicyFetchServiceTest {

    @Mock
    lateinit var policyRepository: PolicyRepository

    @Mock
    lateinit var policyApiClient: PolicyApiClient

    @Mock
    lateinit var objectMapper: ObjectMapper

    @Mock
    lateinit var policyElasticSearchService: PolicyElasticSearchService

    @InjectMocks
    lateinit var policyFetchService: PolicyFetchService

    private lateinit var requestDto: PolicyFetchRequestDto
    private lateinit var responseDto: PolicyFetchResponseDto

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        requestDto = PolicyFetchRequestDto("API_KEY", "1", "100", "json")

        val item1 = createPolicyItem("PLCY001", "SBIZ001")
        val item2 = createPolicyItem("PLCY002", "SBIZ002")

        val pagging = Pagging(totCount = 2, pageNum = 1, pageSize = 100)
        val result = PolicyFetchResponseDto.Result(pagging, listOf(item1, item2))

        responseDto = PolicyFetchResponseDto(0, "SUCCESS", result)

        whenever(objectMapper.writeValueAsString(any())).thenReturn("{json}")
        whenever(policyElasticSearchService.reindexAllFromDb()).thenReturn(0L)
    }

    @Test
    fun `신규 정책만 저장된다`() {
        // given
        whenever(
            policyApiClient.fetchPolicyPage(any(), eq(1), eq(100))
        ).thenReturn(responseDto)

        // PLCY001 already exists
        whenever(
            policyRepository.findExistingPlcyNos(mutableSetOf("PLCY001", "PLCY002"))
        ).thenReturn(mutableSetOf("PLCY001"))

        // when
        policyFetchService.fetchAndSavePolicies(requestDto)

        // then
        verify(policyRepository).saveAll(argThat<Iterable<Policy>> { policies ->
            assertThat(policies).hasSize(1)
            val saved = policies.first()
            assertThat(saved.plcyNo).isEqualTo("PLCY002")
            true
        })
    }

    private fun createPolicyItem(plcyNo: String, sbizCd: String) =
        PolicyItem(
            plcyNo,
            null, null, null, null, null, null, null,
            "정책", "키워드", "설명",
            null, null, "지원내용",
            null, "주관기관", null, null,
            "운영기관", null, null,
            "001", "002",
            "20240101", "20241231",
            null, "온라인", null,
            "http://apply", "서류",
            null, null, null, null, null,
            "20", "30", "N",
            "001", "002",
            "0", "5000",
            null, null, null, null, null, null, null, null,
            "12345",
            null, "null", "null",
            "JOB001", "SCH001",
            "20240101",
            null, null,
            sbizCd
        )
}
