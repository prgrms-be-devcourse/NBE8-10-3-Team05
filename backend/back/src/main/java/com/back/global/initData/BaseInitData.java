package com.back.global.initData;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.member.dto.JoinRequest;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.center.repository.CenterRepository;
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository;
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.entity.EstateRegionCache;
import com.back.domain.welfare.estate.repository.EstateRepository;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PolicyRepository policyRepository;
    private final EstateRepository estateRepository;
    private final CenterRepository centerRepository;
    private final LawyerRepository lawyerRepository;
    private final LawyerCrawlerService lawyerCrawlerService;
    private final EstateRegionCache estateRegionCache;

    private final ObjectMapper objectMapper;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            estateRegionCache.init();
            // self.initMember();
            // self.initPolicy();
            // self.initEstate();
            // self.initCenter();
            // self.initLawyer();
        };
    }

    @Transactional
    public void initMember() {
        if (memberRepository.count() >= 50) {
            return;
        }

        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            memberService.join(new JoinRequest(
                    "name" + i,
                    "email" + i + "@gmail.com",
                    "1234",
                    getRandomDate(),
                    String.valueOf(random.nextInt(2))));
        }
    }

    private String getRandomDate() {
        Random random = new Random();
        long start = LocalDate.of(1920, 1, 1).toEpochDay();
        long end = LocalDate.of(2025, 12, 31).toEpochDay();

        long range = end - start;
        long randomDay = start + (long) (random.nextDouble() * range);

        LocalDate birthDate = LocalDate.ofEpochDay(randomDay);

        // 4. yyMMdd 형식으로 만들기
        int year = birthDate.getYear() % 100; // 뒤의 2자리만
        int month = birthDate.getMonthValue();
        int day = birthDate.getDayOfMonth();

        return String.format("%02d%02d%02d", year, month, day);
    }

    @Transactional
    public void initPolicy() {
        if (policyRepository.count() >= 50) {
            return;
        }

        try (InputStream is = getClass().getResourceAsStream("/json/youth_policy_example.json")) {
            PolicyFetchResponseDto response = objectMapper.readValue(is, PolicyFetchResponseDto.class);

            List<Policy> policyList = response.result().youthPolicyList().stream()
                    .map(policyItem -> Policy.from(policyItem, ""))
                    .toList();

            policyRepository.saveAll(policyList);

        } catch (IOException e) {
            throw new ServiceException("500", "policy 초기 데이터 로드 실패", e);
        }
    }

    @Transactional
    public void initEstate() {
        if (estateRepository.count() >= 50) {
            return;
        }

        try (InputStream is = getClass().getResourceAsStream("/json/estate_example.json")) {
            EstateFetchResponseDto response = objectMapper.readValue(is, EstateFetchResponseDto.class);

            List<Estate> estateList =
                    response.response().body().items().stream().map(Estate::new).toList();

            estateRepository.saveAll(estateList);

        } catch (IOException e) {
            throw new ServiceException("500", "estate 초기 데이터 로드 실패", e);
        }
    }

    @Transactional
    public void initCenter() {
        if (centerRepository.count() >= 50) {
            return;
        }

        try (InputStream is = getClass().getResourceAsStream("/json/center_example.json")) {
            CenterApiResponseDto response = objectMapper.readValue(is, CenterApiResponseDto.class);

            List<Center> centerList =
                    response.data().stream().map(Center::dtoToEntity).toList();

            centerRepository.saveAll(centerList);

        } catch (IOException e) {
            throw new ServiceException("500", "center 초기 데이터 로드 실패", e);
        }
    }

    @Transactional
    public void initLawyer() {
        if (lawyerRepository.count() >= 1) {
            return;
        }

        // lawyerCrawlerService.crawlAllPages();
        // lawyerCrawlerService.crawlMultiPages("서울", 1, 1);

        if (lawyerRepository.count() < 1) {
            throw new ServiceException("500", "InitData lawyer 초기 데이터 로드 실패");
        }
    }
}
