package com.back.domain.welfare.center.center.dto

data class CenterApiRequestDto(
    val page: Int?,  // page index (default = 1)
    val perPage: Int?,  // pageSize (default = 10)
    val returnType: String? // json xml (default = json)
) {
    companion object {
        fun from(pageNum: Int, pageSize: Int): CenterApiRequestDto {
            return CenterApiRequestDto(
                if (pageNum > 0) pageNum else 1,  // page 기본값 1
                if (pageSize > 0) pageSize else 10,  // perPage 기본값 10
                "json" // returnType 기본값 json
            )
        }
    }
}
