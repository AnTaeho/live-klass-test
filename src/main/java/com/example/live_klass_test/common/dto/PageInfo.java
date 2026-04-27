package com.example.live_klass_test.common.dto;

public record PageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
