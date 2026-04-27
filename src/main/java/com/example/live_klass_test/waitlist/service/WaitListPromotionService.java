package com.example.live_klass_test.waitlist.service;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.waitlist.domain.WaitList;
import com.example.live_klass_test.waitlist.domain.WaitListStatus;
import com.example.live_klass_test.waitlist.repository.WaitListRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitListPromotionService {

    private final WaitListRepository waitListRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LiveClassRepository liveClassRepository;

    @Transactional
    public void promoteNextInLine(Long liveClassId) {

        // 1) 대기자 없으면 조기 반환 — 불필요한 락 없이 빠르게 탈출
        Optional<WaitList> nextWaiting = waitListRepository
                .findFirstByLiveClassIdAndStatusOrderByPositionAsc(liveClassId, WaitListStatus.WAITING);
        if (nextWaiting.isEmpty()) {
            return;
        }

        // 2) 락 획득 → 정원 확인을 직렬화하여 중복 승격 방지
        LiveClass liveClass = liveClassRepository.findByIdWithLock(liveClassId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 존재하지 않습니다."));

        long currentCount = enrollmentRepository.countByLiveClassIdAndStatusNot(
                liveClassId, EnrollmentStatus.CANCELLED);
        if (liveClass.isOverCapacity(currentCount)) {
            return;
        }

        WaitList waitList = nextWaiting.get();
        Enrollment enrollment = new Enrollment(waitList.getMember(), liveClass);
        enrollmentRepository.save(enrollment);
        waitList.promote(enrollment);
    }
}
