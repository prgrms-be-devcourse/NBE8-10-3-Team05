package com.back.domain.member.geo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeoApiResponseDto(Meta meta, List<Document> documents) {
    public record Meta(
            @JsonProperty("total_count") int totalCount,
            @JsonProperty("pageable_count") int pageableCount,
            @JsonProperty("is_end") boolean isEnd) {}

    public record Document(
            @JsonProperty("address_name") String addressName,
            String x, // 경도
            String y, // 위도
            @JsonProperty("address_type") String addressType,
            Address address,
            @JsonProperty("road_address") RoadAddress roadAddress) {}

    public record Address(
            @JsonProperty("address_name") String addressName,
            @JsonProperty("region_1depth_name") String region1depthName,
            @JsonProperty("region_2depth_name") String region2depthName,
            @JsonProperty("region_3depth_name") String region3depthName,
            @JsonProperty("h_code") String hCode, // 행정동 코드
            @JsonProperty("b_code") String bCode, // 법정동 코드
            String x,
            String y) {}

    public record RoadAddress(
            @JsonProperty("address_name") String addressName,
            @JsonProperty("zone_no") String zoneNo, // 우편번호
            String x,
            String y) {}
}
