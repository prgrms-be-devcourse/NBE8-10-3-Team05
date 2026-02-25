package com.back.domain.welfare.estate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.back.domain.welfare.estate.entity.Estate;

@Repository
public interface EstateRepository extends JpaRepository<Estate, Integer> {
    @Deprecated
    List<Estate> findByBrtcNmContaining(String sido);

    @Deprecated
    List<Estate> findByBrtcNmContainingAndSignguNmContaining(String sido, String signguNm);

    @Query("SELECT DISTINCT e.brtcNm FROM Estate e")
    List<String> findDistinctBrtcNmBy();

    @Query("SELECT DISTINCT e.brtcNm, e.signguNm FROM Estate e")
    List<Object[]> findDistinctBrtcNmAndSignguNmBy();

    // TODO: 기능구현을 위해 임시로 query작성한 것 추후 리팩토링 필요
    @Query("SELECT e FROM Estate e WHERE "
            + "(e.brtcNm LIKE %:k1% OR e.signguNm LIKE %:k1% OR e.fullAdres LIKE %:k1%) AND "
            + "(e.brtcNm LIKE %:k2% OR e.signguNm LIKE %:k2% OR e.fullAdres LIKE %:k2%)")
    List<Estate> searchByKeywords(String k1, String k2);
}
