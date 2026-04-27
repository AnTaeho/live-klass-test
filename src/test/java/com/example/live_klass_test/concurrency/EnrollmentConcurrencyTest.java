package com.example.live_klass_test.concurrency;

import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import com.example.live_klass_test.enrollment.dto.EnrollResultResponse;
import com.example.live_klass_test.enrollment.dto.EnrollResultType;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.enrollment.service.EnrollmentService;
import com.example.live_klass_test.enrollment.dto.EnrollmentRequest;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.repository.MemberRepository;
import com.example.live_klass_test.waitlist.domain.WaitListStatus;
import com.example.live_klass_test.waitlist.repository.WaitListRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EnrollmentConcurrencyTest {

    @Autowired EnrollmentService enrollmentService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired WaitListRepository waitListRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired LiveClassRepository liveClassRepository;

    private static final int CAPACITY = 5;
    private static final int THREAD_COUNT = 10;

    private LiveClass targetClass;
    private List<Member> students;

    @BeforeEach
    void setUp() {
        Member creator = memberRepository.save(
                new Member("강사", "conc_creator@example.com", MemberRole.CREATOR));

        targetClass = new LiveClass("동시성 테스트 강의", "설명", 10000, CAPACITY,
                LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), creator);
        targetClass.open();
        liveClassRepository.save(targetClass);

        students = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            students.add(memberRepository.save(
                    new Member("학생" + i, "conc_s" + i + "@example.com", MemberRole.STUDENT)));
        }
    }

    @Test
    @DisplayName("동시에 10명 수강 신청 - 정원 5명: 5명 등록, 5명 대기열")
    void concurrentEnroll_exactCapacity() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);

        AtomicInteger enrolledCount = new AtomicInteger();
        AtomicInteger waitlistedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Long studentId = students.get(i).getId();
            final Long classId = targetClass.getId();

            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    EnrollResultResponse result = enrollmentService.enroll(
                            new EnrollmentRequest(classId, studentId));
                    if (result.resultType() == EnrollResultType.ENROLLED) {
                        enrolledCount.incrementAndGet();
                    } else {
                        waitlistedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        // 정확히 5명만 ENROLLED, 나머지 5명은 WAITLISTED (혹은 에러 없음)
        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(enrolledCount.get()).isEqualTo(CAPACITY);
        assertThat(waitlistedCount.get()).isEqualTo(THREAD_COUNT - CAPACITY);

        // DB에서 실제 상태 검증
        long confirmedOrPending = enrollmentRepository.countByLiveClassIdAndStatusNot(
                targetClass.getId(), EnrollmentStatus.CANCELLED);
        assertThat(confirmedOrPending).isEqualTo(CAPACITY);

        long waitingCount = waitListRepository
                .findByMemberIdWithClass(students.get(0).getId()).stream()
                .filter(w -> w.getStatus() == WaitListStatus.WAITING)
                .count();
        // 전체 WAITING 대기열 수 검증
        long totalWaiting = students.stream()
                .flatMap(s -> waitListRepository.findByMemberIdWithClass(s.getId()).stream())
                .filter(w -> w.getStatus() == WaitListStatus.WAITING)
                .count();
        assertThat(totalWaiting).isEqualTo(THREAD_COUNT - CAPACITY);
    }

    @AfterEach
    void tearDown() {
        // 외래 키 의존 순서: WaitList → Enrollment → LiveClass → Member
        waitListRepository.deleteAll();
        enrollmentRepository.deleteAll();
        liveClassRepository.deleteAll();
        memberRepository.deleteAll();
    }
}
