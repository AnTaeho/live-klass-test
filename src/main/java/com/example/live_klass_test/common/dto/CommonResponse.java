package com.example.live_klass_test.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T>(
        T result,
        PageInfo page
) {

    public CommonResponse(T result) {
        this(result, null);
    }

    public static <T> CommonResponse<List<T>> of(Page<T> page) {
        return new CommonResponse<>(
                page.getContent(),
                new PageInfo(page.getNumber(), page.getSize(),
                        page.getTotalElements(), page.getTotalPages())
        );
    }

    public static final CommonResponse<EmptyDto> EMPTY = new CommonResponse<>(new EmptyDto());
}
