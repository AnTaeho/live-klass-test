package com.example.live_klass_test.enrollment.controller;

import com.example.live_klass_test.common.dto.CommonResponse;
import com.example.live_klass_test.common.dto.EmptyDto;
import com.example.live_klass_test.enrollment.dto.ClassStudentResponse;
import com.example.live_klass_test.enrollment.dto.EnrollResultResponse;
import com.example.live_klass_test.enrollment.dto.EnrollmentCancelRequest;
import com.example.live_klass_test.enrollment.dto.EnrollmentRequest;
import com.example.live_klass_test.enrollment.dto.EnrollmentResponse;
import com.example.live_klass_test.enrollment.dto.MyEnrollmentResponse;
import com.example.live_klass_test.enrollment.dto.PayRequest;
import com.example.live_klass_test.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<EnrollResultResponse> enroll(@RequestBody @Valid EnrollmentRequest request) {
        return new CommonResponse<>(enrollmentService.enroll(request));
    }

    @DeleteMapping("/{enrollmentId}")
    public CommonResponse<EmptyDto> cancel(
            @PathVariable Long enrollmentId,
            @RequestParam Long memberId) {
        enrollmentService.cancel(new EnrollmentCancelRequest(memberId, enrollmentId));
        return CommonResponse.EMPTY;
    }

    @GetMapping("/my/{memberId}")
    public CommonResponse<List<MyEnrollmentResponse>> getMyEnrollmentList(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return CommonResponse.of(enrollmentService.getMyEnrollments(memberId, pageable));
    }

    @GetMapping("/class/{classId}")
    public CommonResponse<List<ClassStudentResponse>> getClassStudents(
            @PathVariable Long classId,
            @RequestParam Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return CommonResponse.of(enrollmentService.getClassStudents(classId, creatorId, pageable));
    }

    @PostMapping("/payment")
    public CommonResponse<EnrollmentResponse> pay(@RequestBody @Valid PayRequest request) {
        return new CommonResponse<>(enrollmentService.pay(request));
    }
}
