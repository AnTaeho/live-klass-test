package com.example.live_klass_test.enrollment;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import com.example.live_klass_test.enrollment.dto.EnrollResultResponse;
import com.example.live_klass_test.enrollment.dto.EnrollResultType;
import com.example.live_klass_test.enrollment.dto.EnrollmentCancelRequest;
import com.example.live_klass_test.enrollment.dto.EnrollmentRequest;
import com.example.live_klass_test.enrollment.dto.PayRequest;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.enrollment.service.EnrollmentService;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnrollmentServiceTest {

    @Autowired EnrollmentService enrollmentService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired LiveClassRepository liveClassRepository;
    @Autowired EntityManager em;

    private Member creator;
    private Member student;
    private LiveClass openClass;

    @BeforeEach
    void setUp() {
        creator = memberRepository.save(new Member("강사", "enroll_creator@example.com", MemberRole.CREATOR));
        student = memberRepository.save(new Member("학생", "enroll_student@example.com", MemberRole.STUDENT));

        openClass = new LiveClass("수강 강의", "설명", 30000, 5,
                LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), creator);
        openClass.open();
        liveClassRepository.save(openClass);
    }

    @Test
    @DisplayName("수강 신청 성공 - ENROLLED 반환")
    void enroll_success_enrolled() {
        EnrollResultResponse response = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));

        assertThat(response.resultType()).isEqualTo(EnrollResultType.ENROLLED);
        assertThat(response.resultId()).isNotNull();
        assertThat(response.waitListPosition()).isNull();
    }

    @Test
    @DisplayName("정원 초과 시 대기열 등록 - WAITLISTED 반환")
    void enroll_overCapacity_waitlisted() {
        // 정원 1짜리 강의 생성
        LiveClass smallClass = new LiveClass("소규모", "설명", 10000, 1,
                LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), creator);
        smallClass.open();
        liveClassRepository.save(smallClass);

        Member firstStudent = memberRepository.save(new Member("학생1", "first@example.com", MemberRole.STUDENT));
        Member secondStudent = memberRepository.save(new Member("학생2", "second@example.com", MemberRole.STUDENT));

        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), firstStudent.getId()));
        EnrollResultResponse result = enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), secondStudent.getId()));

        assertThat(result.resultType()).isEqualTo(EnrollResultType.WAITLISTED);
        assertThat(result.waitListPosition()).isEqualTo(1);
    }

    @Test
    @DisplayName("OPEN이 아닌 강의에 수강 신청 시 예외")
    void enroll_nonOpenClass_throws() {
        LiveClass draftClass = new LiveClass("드래프트 강의", "설명", 10000, 5,
                LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), creator);
        liveClassRepository.save(draftClass);

        assertThatThrownBy(() -> enrollmentService.enroll(
                new EnrollmentRequest(draftClass.getId(), student.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OPEN 상태인 강의만");
    }

    @Test
    @DisplayName("중복 수강 신청 시 예외")
    void enroll_duplicate_throws() {
        enrollmentService.enroll(new EnrollmentRequest(openClass.getId(), student.getId()));

        assertThatThrownBy(() -> enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 수강 신청한 강의");
    }

    @Test
    @DisplayName("PENDING 상태 수강 취소 성공")
    void cancel_pending_success() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));

        enrollmentService.cancel(new EnrollmentCancelRequest(student.getId(), enrolled.resultId()));

        Enrollment enrollment = enrollmentRepository.findById(enrolled.resultId()).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 7일 이내 취소 성공")
    void cancel_confirmed_withinPeriod_success() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));
        enrollmentService.pay(new PayRequest(student.getId(), enrolled.resultId()));

        enrollmentService.cancel(new EnrollmentCancelRequest(student.getId(), enrolled.resultId()));

        Enrollment enrollment = enrollmentRepository.findById(enrolled.resultId()).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 7일 이후 취소 시 예외")
    void cancel_confirmed_expiredPeriod_throws() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));
        enrollmentService.pay(new PayRequest(student.getId(), enrolled.resultId()));

        // confirmedAt을 8일 전으로 설정
        em.createQuery("UPDATE Enrollment e SET e.confirmedAt = :time WHERE e.id = :id")
                .setParameter("time", LocalDateTime.now().minusDays(8))
                .setParameter("id", enrolled.resultId())
                .executeUpdate();
        em.flush();
        em.clear();

        assertThatThrownBy(() -> enrollmentService.cancel(
                new EnrollmentCancelRequest(student.getId(), enrolled.resultId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("취소 가능 기간");
    }

    @Test
    @DisplayName("다른 사람의 수강 취소 시 예외")
    void cancel_otherMember_throws() {
        Member otherStudent = memberRepository.save(new Member("다른학생", "other_s@example.com", MemberRole.STUDENT));
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));

        assertThatThrownBy(() -> enrollmentService.cancel(
                new EnrollmentCancelRequest(otherStudent.getId(), enrolled.resultId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 수강 신청만");
    }

    @Test
    @DisplayName("이미 취소된 수강 신청 재취소 시 예외")
    void cancel_alreadyCancelled_throws() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));
        enrollmentService.cancel(new EnrollmentCancelRequest(student.getId(), enrolled.resultId()));

        assertThatThrownBy(() -> enrollmentService.cancel(
                new EnrollmentCancelRequest(student.getId(), enrolled.resultId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 취소된");
    }

    @Test
    @DisplayName("결제 성공 - PENDING → CONFIRMED")
    void pay_success() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));

        enrollmentService.pay(new PayRequest(student.getId(), enrolled.resultId()));

        Enrollment enrollment = enrollmentRepository.findById(enrolled.resultId()).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(enrollment.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("중복 결제 시 예외")
    void pay_duplicate_throws() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));
        enrollmentService.pay(new PayRequest(student.getId(), enrolled.resultId()));

        assertThatThrownBy(() -> enrollmentService.pay(
                new PayRequest(student.getId(), enrolled.resultId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 결제된");
    }

    @Test
    @DisplayName("다른 사람의 수강 신청 결제 시 예외")
    void pay_otherMember_throws() {
        Member otherStudent = memberRepository.save(new Member("다른학생", "pay_other@example.com", MemberRole.STUDENT));
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));

        assertThatThrownBy(() -> enrollmentService.pay(
                new PayRequest(otherStudent.getId(), enrolled.resultId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 수강 신청만");
    }

    @Test
    @DisplayName("PENDING이 아닌 수강 신청 결제 시 예외")
    void pay_nonPending_throws() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(openClass.getId(), student.getId()));
        enrollmentService.cancel(new EnrollmentCancelRequest(student.getId(), enrolled.resultId()));

        assertThatThrownBy(() -> enrollmentService.pay(
                new PayRequest(student.getId(), enrolled.resultId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING 상태에서만");
    }
}
