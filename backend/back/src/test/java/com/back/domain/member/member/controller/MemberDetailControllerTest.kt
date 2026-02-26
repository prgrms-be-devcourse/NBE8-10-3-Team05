package com.back.domain.member.member.controller

import com.back.domain.member.geo.dto.AddressDto
import com.back.domain.member.geo.entity.Address
import com.back.domain.member.geo.service.GeoService
import com.back.domain.member.member.dto.MemberDetailReq
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.Member.Companion.createEmailUser
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.enumtype.EducationLevel
import com.back.global.enumtype.EmploymentStatus
import com.back.global.enumtype.MarriageStatus
import com.back.global.enumtype.SpecialStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.List

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberDetailControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var geoService: GeoService

    private fun setupAuth(memberId: Long) {
        val auth = UsernamePasswordAuthenticationToken(
            memberId, null, listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    @DisplayName("내 상세 정보 조회")
    fun getDetail() {
        val member = createEmailUser("홍길동", "me_test@example.com", "1234", "991231", "1")
        val saved = memberRepository.save<Member>(member)
        setupAuth(saved.id!!)

        mvc.perform(MockMvcRequestBuilders.get("/api/v1/member/member/detail"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("홍길동"))
    }

    @Test
    @DisplayName("내 상세 정보 수정")
    @Throws(Exception::class)
    fun modifyDetail() {
        val member = Member.createEmailUser("홍길동", "me_test@example.com", passwordEncoder.encode("12345678")!!, "991231", "1")
        val saved = memberRepository.save(member)
        setupAuth(saved.id!!)

        // 수정 데이터
        val request = MemberDetailReq(
            "홍길동 수정",
            "me_test@example.com",
            "991231",
            "1",
            Member.LoginType.EMAIL,
            Member.Role.USER,
            "54321",
            MarriageStatus.MARRIED,
            5000,
            EmploymentStatus.EMPLOYED,
            EducationLevel.UNIVERSITY_GRADUATED,
            SpecialStatus.BASIC_LIVELIHOOD
        )

        mvc.perform(
            MockMvcRequestBuilders.put("/api/v1/member/member/detail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("홍길동 수정"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.regionCode").value("54321"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(5000))
    }

    @Test
    @DisplayName("상세 정보 없는 멤버 정보 조회 -> MemberDetail 자동 생성 & null 값인지 확인")
    fun autoCreateDetail() {
        val newMember = Member.createEmailUser("신규회원", "new@email.com", passwordEncoder.encode("pass")!!, "991231", "1")
        val savedNewMember = memberRepository.save(newMember)
        setupAuth(savedNewMember.id!!)

        mvc.perform(MockMvcRequestBuilders.get("/api/v1/member/member/detail"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk()) // Member 기본 정보 확인
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("신규회원"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.email").value("new@email.com")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.regionCode").value(Matchers.nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.income").value(Matchers.nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.marriageStatus").value(Matchers.nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.employmentStatus").value(Matchers.nullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.educationLevel").value(Matchers.nullValue()))
    }

    @Test
    @DisplayName("주소 업데이트 성공 테스트")
    @Throws(Exception::class)
    fun updateAddress_Success() {
        val member = Member.createEmailUser("테스트", "test@test.com", passwordEncoder.encode("pass")!!, "991231", "1")
        val saved = memberRepository.save(member)
        setupAuth(saved.id!!)

        // 요청 데이터 준비
        val requestBody = Address.builder()
            .postcode("12345")
            .roadAddress("서울특별시 강남구 테헤란로 427")
            .build()

        val enrichedDto = Address.builder()
            .postcode("12345")
            .addressName("서울특별시 강남구 테헤란로 427")
            .hCode("4514069000")
            .latitude(37.503)
            .longitude(127.044)
            .build()

        BDDMockito.given(geoService.getGeoCode(any()))
            .willReturn(enrichedDto)

        mvc.perform(
            MockMvcRequestBuilders.put("/api/v1/member/member/detail/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk()) // 응답 값 검증
            .andExpect(MockMvcResultMatchers.jsonPath("$.hCode").value("4514069000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.latitude").value(37.503))
            .andExpect(MockMvcResultMatchers.jsonPath("$.longitude").value(127.044))
    }
}
