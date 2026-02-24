package com.back.domain.member.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.geo.dto.AddressDto;
import com.back.domain.member.geo.entity.Address;
import com.back.domain.member.geo.service.GeoService;
import com.back.domain.member.member.dto.MemberDetailReq;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.enumtype.EducationLevel;
import com.back.global.enumtype.EmploymentStatus;
import com.back.global.enumtype.MarriageStatus;
import com.back.global.enumtype.SpecialStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberDetailControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GeoService geoService;

    @Test
    @DisplayName("내 상세 정보 조회")
    void getDetail() throws Exception {
        Member member = Member.createEmailUser("홍길동", "me_test@example.com", "1234", "991231", "1");
        Member saved = memberRepository.save(member);

        var auth = new UsernamePasswordAuthenticationToken(
                saved.getId(), // ID를 직접 주입
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mvc.perform(get("/api/v1/member/member/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    @DisplayName("내 상세 정보 수정")
    void modifyDetail() throws Exception {
        Member member =
                Member.createEmailUser("홍길동", "me_test@example.com", passwordEncoder.encode("12345678"), "991231", "1");
        Member saved = memberRepository.save(member);

        var auth = new UsernamePasswordAuthenticationToken(
                saved.getId(),
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(auth);

        // 수정 데이터
        MemberDetailReq request = new MemberDetailReq(
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
                SpecialStatus.BASIC_LIVELIHOOD);

        mvc.perform(put("/api/v1/member/member/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동 수정"))
                .andExpect(jsonPath("$.regionCode").value("54321"))
                .andExpect(jsonPath("$.income").value(5000));
    }

    @Test
    @DisplayName("상세 정보 없는 멤버 정보 조회 -> MemberDetail 자동 생성 & null 값인지 확인")
    void autoCreateDetail() throws Exception {

        Member newMember =
                Member.createEmailUser("신규회원", "new@email.com", passwordEncoder.encode("pass"), "991231", "1");
        Member savedNewMember = memberRepository.save(newMember);

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                savedNewMember.getId(),
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(auth);

        mvc.perform(get("/api/v1/member/member/detail"))
                .andDo(print())
                .andExpect(status().isOk())
                // Member 기본 정보 확인
                .andExpect(jsonPath("$.name").value("신규회원"))
                .andExpect(jsonPath("$.email").value("new@email.com"))
                // MemberDetail 자동 생성 및 초기값(null) 확인
                .andExpect(jsonPath("$.regionCode").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.income").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.marriageStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.employmentStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.educationLevel").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("주소 업데이트 성공 테스트")
    void updateAddress_Success() throws Exception {
        Member member = Member.createEmailUser("테스트", "test@test.com", passwordEncoder.encode("pass"), "991231", "1");
        Member saved = memberRepository.save(member);

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                saved.getId(),
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(auth);

        // 요청 데이터 준비
        Address requestBody = Address.builder()
                .postcode("12345")
                .roadAddress("서울특별시 강남구 테헤란로 427")
                .build();

        Address enrichedDto = Address.builder()
                .postcode("12345")
                .addressName("서울특별시 강남구 테헤란로 427")
                .hCode("4514069000")
                .latitude(37.503)
                .longitude(127.044)
                .build();

        given(geoService.getGeoCode(any(AddressDto.class))).willReturn(enrichedDto);

        mvc.perform(put("/api/v1/member/member/detail/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))) // Address 객체 전달
                .andDo(print())
                .andExpect(status().isOk())
                // 응답 값 검증
                .andExpect(jsonPath("$.hCode").value("4514069000"))
                .andExpect(jsonPath("$.latitude").value(37.503))
                .andExpect(jsonPath("$.longitude").value(127.044));
    }
}
