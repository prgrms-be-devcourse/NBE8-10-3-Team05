package com.back.domain.welfare.center.center.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.center.center.entity.Center;

public interface CenterRepository extends JpaRepository<Center, Integer> {
    List<Center> findByLocation(String sido);

    List<Center> findByAddressContaining(String sido);
}
