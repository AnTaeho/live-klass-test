package com.example.live_klass_test.enrollment.dto;

public record EnrollResultResponse(
        EnrollResultType resultType,
        Long resultId,
        Integer waitListPosition
) {
    public static EnrollResultResponse enrolled(Long enrollmentId) {
        return new EnrollResultResponse(EnrollResultType.ENROLLED, enrollmentId, null);
    }

    public static EnrollResultResponse waitlisted(Long waitListId, int position) {
        return new EnrollResultResponse(EnrollResultType.WAITLISTED, waitListId, position);
    }
}
