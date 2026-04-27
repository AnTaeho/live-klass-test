package com.example.live_klass_test.liveclass.dto;

import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import java.time.LocalDate;

public record ClassDetailResponse(
        Long id,
        String title,
        String description,
        int price,
        int maxCapacity,
        long currentEnrollmentCount,
        LocalDate startDate,
        LocalDate endDate,
        ClassStatus status,
        String creatorName
) {
    public static ClassDetailResponse from(LiveClass liveClass, long currentEnrollmentCount) {
        return new ClassDetailResponse(
                liveClass.getId(),
                liveClass.getTitle(),
                liveClass.getDescription(),
                liveClass.getPrice(),
                liveClass.getMaxCapacity(),
                currentEnrollmentCount,
                liveClass.getStartDate(),
                liveClass.getEndDate(),
                liveClass.getStatus(),
                liveClass.getCreator().getName()
        );
    }
}
