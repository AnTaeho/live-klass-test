package com.example.live_klass_test.enrollment.dto;

import com.example.live_klass_test.enrollment.domain.Enrollment;

public record EnrollmentResponse(Long enrollmentId) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.getId());
    }
}
