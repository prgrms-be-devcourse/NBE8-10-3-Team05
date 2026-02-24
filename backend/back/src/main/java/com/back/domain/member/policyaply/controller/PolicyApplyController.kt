package com.back.domain.member.policyaply.controller

import com.back.domain.member.policyaply.dto.AddApplicationResponseDto
import com.back.domain.member.policyaply.dto.DeleteApplicationResponseDto
import com.back.domain.member.policyaply.entity.Application
import com.back.domain.member.policyaply.service.PolicyApplyService
import com.back.standard.util.ActorProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/member/policy-aply")
class PolicyApplyController(
    private val policyApplyService: PolicyApplyService,
    private val actorProvider: ActorProvider
) {

    @GetMapping("/welfare-applications")
    fun getApplicationList(): ResponseEntity<*> {
        val member = actorProvider.getActor()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AddApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요"))

        val applications = policyApplyService.getApplicationList(member)
        return ResponseEntity.ok(applications)
    }

    @PostMapping("/welfare-application/{policyId}")
    fun addApplication(@PathVariable policyId: Int): ResponseEntity<AddApplicationResponseDto> {
        val member = actorProvider.getActor()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AddApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요"))

        val application = policyApplyService.addApplication(member, policyId)

        return if (application != null) {
            ResponseEntity.ok(AddApplicationResponseDto(HttpStatus.OK.value(), "저장되었습니다!"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(AddApplicationResponseDto(HttpStatus.NOT_FOUND.value(), "존재하지 않는 정책입니다."))
        }
    }

    @PutMapping("/welfare-application/{id}")
    fun deleteApplication(@PathVariable id: Long): ResponseEntity<DeleteApplicationResponseDto> {
        val member = actorProvider.getActor()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(DeleteApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요"))

        val responseDto = policyApplyService.deleteApplication(member, id)
        return ResponseEntity.status(responseDto.code).body(responseDto)
    }
}
