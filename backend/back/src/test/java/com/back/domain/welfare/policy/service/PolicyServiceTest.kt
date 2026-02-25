package com.back.domain.welfare.policy.service

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto
import com.back.domain.welfare.policy.repository.PolicyRepositoryCustom
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PolicyServiceTest {

    @Mock
    lateinit var policyRepository: PolicyRepositoryCustom

    @InjectMocks
    lateinit var policyService: PolicyService

    private lateinit var requestDto: PolicySearchRequestDto
    private lateinit var responseDto: PolicySearchResponseDto

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        requestDto = PolicySearchRequestDto(
            sprtTrgtMinAge = 20,
            sprtTrgtMaxAge = 30,
            zipCd = "12345",
            schoolCd = "SCH001",
            jobCd = "JOB001",
            earnMinAmt = 2000,
            earnMaxAmt = 5000,
        )

        responseDto = PolicySearchResponseDto(
            id = 1,
            plcyNo = "PLCY001",
            plcyNm = "Test Policy",
            plcyExplnCn = "설명",
            plcySprtCn = "지원 내용",
            plcyKywdNm = "키워드",
            sprtTrgtMinAge = "20",
            sprtTrgtMaxAge = "30",
            zipCd = "12345",
            schoolCd = "SCH001",
            jobCd = "JOB001",
            earnMinAmt = "2000",
            earnMaxAmt = "5000",
            aplyYmd = "20260101",
            aplyUrlAddr = "http://apply.url",
            aplyMthdCn = "온라인",
            sbmsnDcmntCn = "서류",
            operInstCdNm = "운영기관",
        )
    }

    @Test
    fun `search - 정책 목록을 반환한다`() {
        // given
        whenever(policyRepository.search(requestDto)).thenReturn(listOf(responseDto))

        // when
        val result = policyService.search(requestDto)

        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().plcyNm).isEqualTo("Test Policy")

        verify(policyRepository).search(requestDto)
    }
}
