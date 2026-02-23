package com.back.domain.welfare.center.center.dto;

public record CenterApiRequestDto(
        Integer page, // page index (default = 1)
        Integer perPage, // pageSize (default = 10)
        String returnType // json xml (default = json)
        ) {
    public static CenterApiRequestDto from(int pageNum, int pageSize) {
        return new CenterApiRequestDto(
                pageNum > 0 ? pageNum : 1, // page 기본값 1
                pageSize > 0 ? pageSize : 10, // perPage 기본값 10
                "json" // returnType 기본값 json
                );
    }
}
