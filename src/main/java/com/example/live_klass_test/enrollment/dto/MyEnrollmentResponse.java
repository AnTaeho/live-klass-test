package com.example.live_klass_test.enrollment.dto;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;

public record MyEnrollmentResponse(
        Long enrollmentId,
        String classTitle,
        EnrollmentStatus status
) {
    public static MyEnrollmentResponse from(Enrollment enrollment) {
        return new MyEnrollmentResponse(
                enrollment.getId(),
                enrollment.getLiveClass().getTitle(),
                enrollment.getStatus()
        );
    }
}
