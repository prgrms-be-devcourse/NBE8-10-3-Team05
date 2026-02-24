package com.back.domain.member.policyaply.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.policyaply.dto.AddApplicationResponseDto;
import com.back.domain.member.policyaply.dto.DeleteApplicationResponseDto;
import com.back.domain.member.policyaply.entity.Application;
import com.back.domain.member.policyaply.service.PolicyApplyService;
import com.back.standard.util.ActorProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/policy-aply")
@RequiredArgsConstructor
public class PolicyApplyController {

    private final PolicyApplyService policyApplyService;
    private final MemberRepository memberRepository;
    private final ActorProvider actorProvider;

    @GetMapping("/welfare-applications")
    public ResponseEntity<?> getApplicationList() {
        Member member = actorProvider.getActor();

        if (member == null) {
            AddApplicationResponseDto responseDto =
                    new AddApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }

        List<Application> applications = policyApplyService.getApplicationList(member);
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/welfare-application/{policyId}")
    public ResponseEntity<AddApplicationResponseDto> addApplication(@PathVariable Integer policyId) {

        Member member = actorProvider.getActor();

        if (member == null) {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(addApplicationResponseDto);
        }

        Application application = policyApplyService.addApplication(member, policyId);

        if (application != null) {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.OK.value(), "저장되었습니다!");
            return ResponseEntity.ok(addApplicationResponseDto);
        } else {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.NOT_FOUND.value(), "존재하지 않는 정책입니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(addApplicationResponseDto);
        }
    }

    @PutMapping("/welfare-application/{id}")
    public ResponseEntity<DeleteApplicationResponseDto> deleteAplication(@PathVariable long id) {

        Member member = actorProvider.getActor();
        if (member == null) {
            DeleteApplicationResponseDto deleteApplicationResponseDto =
                    new DeleteApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(deleteApplicationResponseDto);
        }

        DeleteApplicationResponseDto deleteApplicationResponseDto = policyApplyService.deleteApplication(member, id);

        return ResponseEntity.status(deleteApplicationResponseDto.code).body(deleteApplicationResponseDto);
    }
}
