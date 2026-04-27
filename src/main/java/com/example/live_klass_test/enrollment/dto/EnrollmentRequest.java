package com.example.live_klass_test.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "강의 ID는 필수입니다.")
        Long liveClassId,

        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId
) {
}
