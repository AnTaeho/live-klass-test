package com.example.live_klass_test.enrollment.dto;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import java.time.LocalDateTime;

public record ClassStudentResponse(
        Long enrollmentId,
        Long memberId,
        String memberName,
        String memberEmail,
        EnrollmentStatus status,
        LocalDateTime enrolledAt
) {
    public static ClassStudentResponse from(Enrollment enrollment) {
        return new ClassStudentResponse(
                enrollment.getId(),
                enrollment.getMember().getId(),
                enrollment.getMember().getName(),
                enrollment.getMember().getEmail(),
                enrollment.getStatus(),
                enrollment.getCreatedAt()
        );
    }
}
