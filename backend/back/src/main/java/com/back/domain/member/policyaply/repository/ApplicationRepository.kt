package com.back.domain.member.policyaply.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.member.policyaply.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findAllByApplicant_Id(Long applicantId);

    Application getApplicationById(Long id);
}
