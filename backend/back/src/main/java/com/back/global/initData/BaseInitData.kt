package com.back.global.initData

import com.back.domain.member.member.dto.JoinRequest
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto
import com.back.domain.welfare.center.center.entity.Center
import com.back.domain.welfare.center.center.repository.CenterRepository
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService
import com.back.domain.welfare.estate.dto.EstateDto
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto
import com.back.domain.welfare.estate.entity.Estate
import com.back.domain.welfare.estate.entity.EstateRegionCache
import com.back.domain.welfare.estate.repository.EstateRepository
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem
import com.back.domain.welfare.policy.entity.Policy
import com.back.domain.welfare.policy.repository.PolicyRepository
import com.back.global.exception.ServiceException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.time.LocalDate
import java.util.*

@Configuration
@Profile("dev")
class BaseInitData(
    private val memberService: MemberService,
    private val memberRepository: MemberRepository,
    private val policyRepository: PolicyRepository,
    private val estateRepository: EstateRepository,
    private val centerRepository: CenterRepository,
    private val lawyerRepository: LawyerRepository,
    private val lawyerCrawlerService: LawyerCrawlerService?,
    private val estateRegionCache: EstateRegionCache,
    private val objectMapper: ObjectMapper
) {
    @Autowired
    @Lazy
    private val self: BaseInitData? = null

    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments? ->
            estateRegionCache.init()
        }
    }

    @Transactional
    fun initMember() {
        if (memberRepository.count() >= 50) {
            return
        }

        val random = Random()

        for (i in 0..49) {
            memberService.join(
                JoinRequest(
                    "name" + i,
                    "email" + i + "@gmail.com",
                    "1234",
                    this.randomDate,
                    random.nextInt(2).toString()
                )
            )
        }
    }

    private val randomDate: String
        get() {
            val random = Random()
            val start = LocalDate.of(1920, 1, 1).toEpochDay()
            val end = LocalDate.of(2025, 12, 31).toEpochDay()

            val range = end - start
            val randomDay = start + (random.nextDouble() * range).toLong()

            val birthDate = LocalDate.ofEpochDay(randomDay)

            // 4. yyMMdd 형식으로 만들기
            val year = birthDate.year % 100 // 뒤의 2자리만
            val month = birthDate.monthValue
            val day = birthDate.dayOfMonth

            return String.format("%02d%02d%02d", year, month, day)
        }

    @Transactional
    fun initPolicy() {
        if (policyRepository.count() >= 50) {
            return
        }

        try {
            javaClass.getResourceAsStream("/json/youth_policy_example.json").use { `is` ->
                val response = objectMapper.readValue(`is`, PolicyFetchResponseDto::class.java)
                val policyList = response.result()!!.youthPolicyList()!!.stream()
                    .map { policyItem: PolicyItem? -> Policy.from(policyItem!!, "") }
                    .toList()
                policyRepository.saveAll(policyList)
            }
        } catch (e: IOException) {
            throw ServiceException("500", "policy 초기 데이터 로드 실패", e)
        }
    }

    @Transactional
    fun initEstate() {
        if (estateRepository.count() >= 50) {
            return
        }

        try {
            javaClass.getResourceAsStream("/json/estate_example.json").use { `is` ->
                val response = objectMapper.readValue(`is`, EstateFetchResponseDto::class.java)
                val estateList =
                    response.response!!.body!!.items!!.stream().map { dto: EstateDto? -> Estate(dto!!) }
                        .toList()
                estateRepository.saveAll(estateList)
            }
        } catch (e: IOException) {
            throw ServiceException("500", "estate 초기 데이터 로드 실패", e)
        }
    }

    @Transactional
    fun initCenter() {
        if (centerRepository.count() >= 50) {
            return
        }

        try {
            javaClass.getResourceAsStream("/json/center_example.json").use { `is` ->
                val response = objectMapper.readValue(`is`, CenterApiResponseDto::class.java)
                val centerList = response.data.stream()
                    .map { dto: Any? -> Center() } // 원본 로직의 dtoToEntity() 대응 (Center 생성자로 대체 가능성 확인 필요)
                    .toList()
                centerRepository.saveAll(centerList)
            }
        } catch (e: IOException) {
            throw ServiceException("500", "center 초기 데이터 로드 실패", e)
        }
    }

    @Transactional
    fun initLawyer() {
        if (lawyerRepository.count() >= 1) {
            return
        }

        // lawyerCrawlerService.crawlAllPages();
        // lawyerCrawlerService.crawlMultiPages("서울", 1, 1);
        if (lawyerRepository.count() < 1) {
            throw ServiceException("500", "InitData lawyer 초기 데이터 로드 실패")
        }
    }
}
