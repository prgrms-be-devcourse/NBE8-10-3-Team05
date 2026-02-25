package com.back.domain.member.member.controller

import com.back.domain.member.geo.entity.Address
import com.back.domain.member.member.dto.MemberDetailReq
import com.back.domain.member.member.dto.MemberDetailRes
import com.back.domain.member.member.service.MemberDetailService
import com.back.standard.util.ActorProvider
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// 마이그레이션 시 member 과 memberDetail 파트의 충돌 방지를 위해
// memberController에 있는 memberDetail 관련 로직을 분리해 만든 컨트롤러
// 각자 마이그레이션 이 끝나면 추후에 다시 memberController로 통합할 것.
@RestController
@RequestMapping("/api/v1/member/member")
class MemberDetailController(
    private val memberDetailService: MemberDetailService,
    private val actorProvider: ActorProvider
) {

    @GetMapping("/detail")
    fun getMemberDetail(): ResponseEntity<MemberDetailRes> {
        val actor = actorProvider.getActor()

        val response = memberDetailService.getDetail(actor.id!!)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/detail")
    fun modifyMemberDetail(
        @RequestBody @Valid reqBody: MemberDetailReq
    ): ResponseEntity<MemberDetailRes> {
        val actor = actorProvider.getActor()

        memberDetailService.modify(actor.id!!, reqBody)
        val response = memberDetailService.getDetail(actor.id!!)

        return ResponseEntity.ok(response)
    }

    @PutMapping("/detail/address")
    fun updateAddress(@RequestBody @Valid address: Address
    ): ResponseEntity<MemberDetailRes> {
        val actor = actorProvider.getActor()

        memberDetailService.updateAddress(actor.id!!, address)
        val response = memberDetailService.getDetail(actor.id!!)

        return ResponseEntity.ok(response)
    }
}
