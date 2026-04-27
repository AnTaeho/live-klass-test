package com.example.live_klass_test.enrollment.service;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import com.example.live_klass_test.enrollment.dto.ClassStudentResponse;
import com.example.live_klass_test.enrollment.dto.EnrollResultResponse;
import com.example.live_klass_test.enrollment.dto.EnrollmentCancelRequest;
import com.example.live_klass_test.enrollment.dto.EnrollmentRequest;
import com.example.live_klass_test.enrollment.dto.EnrollmentResponse;
import com.example.live_klass_test.enrollment.dto.MyEnrollmentResponse;
import com.example.live_klass_test.enrollment.dto.PayRequest;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.repository.MemberRepository;
import com.example.live_klass_test.payment.domain.Payment;
import com.example.live_klass_test.payment.repository.PaymentRepository;
import com.example.live_klass_test.waitlist.dto.WaitListJoinResponse;
import com.example.live_klass_test.waitlist.service.WaitListPromotionService;
import com.example.live_klass_test.waitlist.service.WaitListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final MemberRepository memberRepository;
    private final LiveClassRepository liveClassRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final WaitListService waitListService;
    private final WaitListPromotionService waitListPromotionService;

    @Transactional
    public EnrollResultResponse enroll(EnrollmentRequest request) {
        Member member = getMember(request);
        LiveClass liveClass = getLiveClass(request);

        checkClassOpen(liveClass);
        checkDoubleEnroll(request);

        long currentCount = enrollmentRepository.countByLiveClassIdAndStatusNot(request.liveClassId(), EnrollmentStatus.CANCELLED);
        if (liveClass.isOverCapacity(currentCount)) {
            WaitListJoinResponse waitListResponse = waitListService.addToWaitList(member, liveClass);
            return EnrollResultResponse.waitlisted(waitListResponse.waitListId(), waitListResponse.position());
        }

        Enrollment enrollment = new Enrollment(member, liveClass);
        enrollmentRepository.save(enrollment);
        return EnrollResultResponse.enrolled(enrollment.getId());
    }

    @Transactional
    public void cancel(EnrollmentCancelRequest request) {
        Enrollment enrollment = getEnrollmentOrThrow(request.enrollmentId());
        validateEnrollmentOwner(enrollment, request.memberId());
        boolean wasConfirmed = enrollment.isConfirmed();
        Long liveClassId = enrollment.getLiveClass().getId();
        enrollment.cancel();
        if (wasConfirmed) {
            cancelPayment(request.enrollmentId(), request.memberId());
        }
        waitListPromotionService.promoteNextInLine(liveClassId);
    }

    private void cancelPayment(Long enrollmentId, Long memberId) {
        Payment payment = paymentRepository.findByMemberIdAndEnrollmentId(memberId, enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제건이 없습니다."));
        payment.cancel();
    }

    public Page<MyEnrollmentResponse> getMyEnrollments(Long memberId, Pageable pageable) {
        return enrollmentRepository.findByMemberIdWithClass(memberId, pageable)
                .map(MyEnrollmentResponse::from);
    }

    public Page<ClassStudentResponse> getClassStudents(Long classId, Long creatorId, Pageable pageable) {
        LiveClass liveClass = liveClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 존재하지 않습니다."));
        if (!liveClass.getCreator().getId().equals(creatorId)) {
            throw new IllegalArgumentException("해당 강의의 크리에이터가 아닙니다.");
        }
        return enrollmentRepository.findByLiveClassIdWithMember(classId, pageable)
                .map(ClassStudentResponse::from);
    }

    @Transactional
    public EnrollmentResponse pay(PayRequest request) {
        Enrollment enrollment = getEnrollmentOrThrow(request.enrollmentId());
        validateEnrollmentOwner(enrollment, request.memberId());
        if (paymentRepository.existsByEnrollmentId(request.enrollmentId())) {
            throw new IllegalStateException("이미 결제된 수강 신청입니다.");
        }

        LiveClass liveClass = enrollment.getLiveClass();
        paymentRepository.save(new Payment(enrollment.getMember().getId(), liveClass.getId(), request.enrollmentId(), liveClass.getPrice()));
        enrollment.completePay();
        return EnrollmentResponse.from(enrollment);
    }

    private Member getMember(EnrollmentRequest request) {
        return memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
    }

    private LiveClass getLiveClass(EnrollmentRequest request) {
        return liveClassRepository.findByIdWithLock(request.liveClassId())
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 존재하지 않습니다."));
    }

    private Enrollment getEnrollmentOrThrow(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 수강 내역이 없습니다."));
    }

    private void validateEnrollmentOwner(Enrollment enrollment, Long memberId) {
        if (!enrollment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 수강 신청만 처리할 수 있습니다.");
        }
    }

    private void checkClassOpen(LiveClass liveClass) {
        if (liveClass.getStatus() != ClassStatus.OPEN) {
            throw new IllegalStateException("OPEN 상태인 강의만 수강신청할 수 있습니다.");
        }
    }

    private void checkDoubleEnroll(EnrollmentRequest request) {
        if (enrollmentRepository.existsByMemberIdAndLiveClassIdAndStatusNot(request.memberId(), request.liveClassId(), EnrollmentStatus.CANCELLED)) {
            throw new IllegalStateException("이미 수강 신청한 강의입니다.");
        }
    }
}
