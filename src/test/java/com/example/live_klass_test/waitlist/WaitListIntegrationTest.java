package com.example.live_klass_test.waitlist;

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
import com.example.live_klass_test.waitlist.domain.WaitList;
import com.example.live_klass_test.waitlist.domain.WaitListStatus;
import com.example.live_klass_test.waitlist.repository.WaitListRepository;
import com.example.live_klass_test.waitlist.service.WaitListService;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
class WaitListIntegrationTest {

    @Autowired EnrollmentService enrollmentService;
    @Autowired WaitListService waitListService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired WaitListRepository waitListRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired LiveClassRepository liveClassRepository;
    @Autowired EntityManager em;

    private Member creator;
    private Member student1;
    private Member student2;
    private Member student3;
    private LiveClass smallClass;

    @BeforeEach
    void setUp() {
        creator = memberRepository.save(new Member("강사", "wl_creator@example.com", MemberRole.CREATOR));
        student1 = memberRepository.save(new Member("학생1", "wl_s1@example.com", MemberRole.STUDENT));
        student2 = memberRepository.save(new Member("학생2", "wl_s2@example.com", MemberRole.STUDENT));
        student3 = memberRepository.save(new Member("학생3", "wl_s3@example.com", MemberRole.STUDENT));

        // 정원 1짜리 강의
        smallClass = new LiveClass("소규모 강의", "설명", 10000, 1,
                LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), creator);
        smallClass.open();
        liveClassRepository.save(smallClass);
    }

    @Test
    @DisplayName("정원 초과 시 대기열 자동 등록")
    void addToWaitList_whenFull() {
        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), student1.getId()));

        EnrollResultResponse result = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));

        assertThat(result.resultType()).isEqualTo(EnrollResultType.WAITLISTED);
        assertThat(result.waitListPosition()).isEqualTo(1);

        WaitList waitList = waitListRepository.findById(result.resultId()).orElseThrow();
        assertThat(waitList.getStatus()).isEqualTo(WaitListStatus.WAITING);
        assertThat(waitList.getMember().getId()).isEqualTo(student2.getId());
    }

    @Test
    @DisplayName("대기열 순서 - 먼저 등록한 사람이 낮은 포지션")
    void waitList_position_ordering() {
        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), student1.getId()));

        EnrollResultResponse r2 = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));
        EnrollResultResponse r3 = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student3.getId()));

        assertThat(r2.waitListPosition()).isEqualTo(1);
        assertThat(r3.waitListPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("수강 취소 시 대기열 1순위가 자동으로 PENDING 상태로 전환")
    void cancel_enrollment_promotesWaitList() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student1.getId()));
        enrollmentService.pay(new PayRequest(student1.getId(), enrolled.resultId()));

        EnrollResultResponse waitlisted = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));

        // student1 취소 → student2 PROMOTED
        enrollmentService.cancel(new EnrollmentCancelRequest(student1.getId(), enrolled.resultId()));
        em.flush();
        em.clear(); // REQUIRES_NEW 커밋 후 L1 캐시 초기화하여 최신 DB 상태 반영

        WaitList waitList = waitListRepository.findById(waitlisted.resultId()).orElseThrow();
        assertThat(waitList.getStatus()).isEqualTo(WaitListStatus.PROMOTED);

        Enrollment promotedEnrollment = waitList.getEnrollment();
        assertThat(promotedEnrollment).isNotNull();
        assertThat(promotedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(promotedEnrollment.getMember().getId()).isEqualTo(student2.getId());
    }

    @Test
    @DisplayName("PENDING 취소 시 대기열 프로모션 - 결제 없이 취소")
    void cancel_pendingEnrollment_promotesWaitList() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student1.getId()));

        EnrollResultResponse waitlisted = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));

        enrollmentService.cancel(new EnrollmentCancelRequest(student1.getId(), enrolled.resultId()));
        em.flush();
        em.clear(); // REQUIRES_NEW 커밋 후 L1 캐시 초기화하여 최신 DB 상태 반영

        WaitList waitList = waitListRepository.findById(waitlisted.resultId()).orElseThrow();
        assertThat(waitList.getStatus()).isEqualTo(WaitListStatus.PROMOTED);
    }

    @Test
    @DisplayName("48시간 초과 대기열 만료 처리 및 다음 순번 프로모션")
    void expireOverduePromotions_andPromoteNext() {
        // student1 수강 신청 → 정원 초과
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student1.getId()));

        // student2 대기열 (position 1)
        EnrollResultResponse waitlisted2 = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));

        // student3 대기열 (position 2)
        EnrollResultResponse waitlisted3 = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student3.getId()));

        // student1 취소 → student2가 PROMOTED 됨
        enrollmentService.cancel(new EnrollmentCancelRequest(student1.getId(), enrolled.resultId()));
        em.flush();
        em.clear(); // REQUIRES_NEW 커밋 후 L1 캐시 초기화하여 최신 DB 상태 반영

        // student2의 promotedAt을 49시간 전으로 업데이트 (만료 시뮬레이션)
        em.createQuery("UPDATE WaitList w SET w.promotedAt = :past WHERE w.id = :id")
                .setParameter("past", LocalDateTime.now().minusHours(49))
                .setParameter("id", waitlisted2.resultId())
                .executeUpdate();
        em.flush();
        em.clear();

        // 만료 처리 실행 → student2 CANCELLED, student3 PROMOTED
        waitListService.expireOverduePromotions();
        em.flush();

        WaitList wl2 = waitListRepository.findById(waitlisted2.resultId()).orElseThrow();
        WaitList wl3 = waitListRepository.findById(waitlisted3.resultId()).orElseThrow();

        assertThat(wl2.getStatus()).isEqualTo(WaitListStatus.CANCELLED);
        assertThat(wl3.getStatus()).isEqualTo(WaitListStatus.PROMOTED);
        assertThat(wl3.getEnrollment().getMember().getId()).isEqualTo(student3.getId());
    }

    @Test
    @DisplayName("대기열 직접 취소")
    void cancelWaitList_success() {
        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), student1.getId()));
        EnrollResultResponse waitlisted = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));

        waitListService.cancelWaitList(waitlisted.resultId(), student2.getId());

        WaitList waitList = waitListRepository.findById(waitlisted.resultId()).orElseThrow();
        assertThat(waitList.getStatus()).isEqualTo(WaitListStatus.CANCELLED);
    }

    @Test
    @DisplayName("다른 사람의 대기열 취소 시 예외")
    void cancelWaitList_otherMember_throws() {
        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), student1.getId()));
        EnrollResultResponse waitlisted = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId()));

        assertThatThrownBy(() -> waitListService.cancelWaitList(waitlisted.resultId(), student3.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 대기열만");
    }

    @Test
    @DisplayName("이미 대기 중인 강의에 재등록 시 예외")
    void addToWaitList_duplicate_throws() {
        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), student1.getId()));
        enrollmentService.enroll(new EnrollmentRequest(smallClass.getId(), student2.getId()));

        assertThatThrownBy(() -> enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 대기 중인 강의");
    }

    @Test
    @DisplayName("대기열이 없을 때 취소해도 정상 처리")
    void cancel_withNoWaitList_noError() {
        EnrollResultResponse enrolled = enrollmentService.enroll(
                new EnrollmentRequest(smallClass.getId(), student1.getId()));

        // 대기열 없이 취소
        enrollmentService.cancel(new EnrollmentCancelRequest(student1.getId(), enrolled.resultId()));

        Enrollment enrollment = enrollmentRepository.findById(enrolled.resultId()).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }
}
