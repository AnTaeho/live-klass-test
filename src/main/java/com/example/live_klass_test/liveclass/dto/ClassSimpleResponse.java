package com.example.live_klass_test.liveclass.dto;

import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import java.time.LocalDate;

public record ClassSimpleResponse(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        ClassStatus status
) {
    public static ClassSimpleResponse from(LiveClass liveClass) {
        return new ClassSimpleResponse(
                liveClass.getId(),
                liveClass.getTitle(),
                liveClass.getStartDate(),
                liveClass.getEndDate(),
                liveClass.getStatus()
        );
    }
}
