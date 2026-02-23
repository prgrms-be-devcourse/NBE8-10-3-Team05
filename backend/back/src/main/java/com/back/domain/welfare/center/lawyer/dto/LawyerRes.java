package com.back.domain.welfare.center.lawyer.dto;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;

public record LawyerRes(String id, String name, String corporation, String districtArea1, String districtArea2) {
    public LawyerRes(Lawyer lawyer) {
        this(
                lawyer.getId(),
                lawyer.getName(),
                lawyer.getCorporation(),
                lawyer.getDistrictArea1(),
                lawyer.getDistrictArea2());
    }
}
