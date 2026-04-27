package com.example.live_klass_test.liveclass.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateClassRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "설명은 필수입니다.")
        String description,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        int price,

        @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
        int maxCapacity,

        @NotNull(message = "시작일은 필수입니다.")
        @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate
) {
    @AssertTrue(message = "종료일은 시작일 이후여야 합니다.")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }
}
