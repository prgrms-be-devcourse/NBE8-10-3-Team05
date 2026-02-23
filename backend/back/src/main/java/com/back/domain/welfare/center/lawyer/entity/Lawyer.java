package com.back.domain.welfare.center.lawyer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lawyer {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String corporation;

    private String districtArea1;
    // 시/도
    private String districtArea2;
    // 군/구

    public void generateId() {
        this.id = String.format("%s_%s_%s_%s", this.name, this.corporation, this.districtArea1, this.districtArea2);
    }
}
