package com.back.domain.member.geo.entity;

import com.back.domain.member.geo.dto.AddressDto;
import com.back.domain.member.geo.dto.GeoApiResponseDto;

import lombok.Builder;

@Builder
public record Address(
        // 카카오 우편번호 검색 API 제공
        String postcode, // 우편번호
        String addressName, // 전체 주소
        String sigunguCode, // 41135 시/군/구 코드
        String bCode, // 4113511000	법정동/법정리 코드
        String roadAddress, // 도로명주소
        String sigungu, // 시/군/구 이름 "성남시 분당구"
        String sido, // 도/시 이름 "경기"

        // 카카오 Local API 제공
        // 도로명 주소로 가져온다.
        String hCode, // "4514069000" 행정동 코드
        Double latitude, // 위도
        Double longitude // 경도
        ) {
    public static Address of(AddressDto base, GeoApiResponseDto.Address geo) {
        return new Address(
                base.postcode(),
                base.addressName(),
                base.sigunguCode(),
                base.bCode(),
                base.roadAddress(),
                base.sigungu(),
                base.sido(),
                geo.hCode(), // 새로운 값 주입
                Double.parseDouble(geo.y()), // 위도 (문자열 -> Double 변환)
                Double.parseDouble(geo.x()) // 경도 (문자열 -> Double 변환)
                );
    }
}
