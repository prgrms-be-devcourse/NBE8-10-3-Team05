package com.back.domain.welfare.estate.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.estate.dto.EstateRegionResponseDto;
import com.back.domain.welfare.estate.dto.EstateSearchResonseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.entity.EstateRegionCache;
import com.back.domain.welfare.estate.service.EstateService;
import com.back.standard.util.SidoNormalizer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/estate")
@RequiredArgsConstructor
public class EstateController {
    private final EstateService estateService;
    private final EstateRegionCache regionCache;

    @GetMapping("/location")
    public EstateSearchResonseDto getEstateLocation(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new EstateSearchResonseDto(estateService.searchEstateLocation("", ""));
        }
        String[] keywords = keyword.split(" ");

        // 서울시 강남구
        // 2개까지만 사용
        // TODO: 추후 확장필요
        String keyword1 = SidoNormalizer.normalizeSido(keywords[0]);
        String keyword2 = (keywords.length >= 2) ? SidoNormalizer.normalizeSido(keywords[1]) : keyword1;

        // TODO: 추후 일반공공 정정공공가 있다면 정정공공만 나오게 하는 로직 추가 필요
        List<Estate> estateList = estateService.searchEstateLocation(keyword1, keyword2);

        return new EstateSearchResonseDto(estateList);
    }

    @GetMapping("/regions")
    public EstateRegionResponseDto getEstateRegions() {
        return new EstateRegionResponseDto(regionCache.getRegionList());
    }
}
