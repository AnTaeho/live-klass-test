package com.example.live_klass_test.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public record PayRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotNull(message = "수강 신청 ID는 필수입니다.")
        Long enrollmentId
) {
}
