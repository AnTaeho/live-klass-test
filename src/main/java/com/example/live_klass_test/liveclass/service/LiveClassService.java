package com.example.live_klass_test.liveclass.service;

import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.dto.ClassDetailResponse;
import com.example.live_klass_test.liveclass.dto.ClassListResponse;
import com.example.live_klass_test.liveclass.dto.ClassSimpleResponse;
import com.example.live_klass_test.liveclass.dto.CreateClassRequest;
import com.example.live_klass_test.liveclass.dto.LiveClassResponse;
import java.time.LocalDate;
import java.util.List;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveClassService {

    private final LiveClassRepository liveClassRepository;
    private final MemberRepository memberRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public LiveClassResponse createClass(CreateClassRequest request, Long creatorId) {
        Member creator = checkCreator(creatorId);
        LiveClass liveClass = new LiveClass(
                request.title(),
                request.description(),
                request.price(),
                request.maxCapacity(),
                request.startDate(),
                request.endDate(),
                creator
        );

        LiveClass savedClass = liveClassRepository.save(liveClass);
        return LiveClassResponse.from(savedClass);
    }

    @Transactional
    public void deleteClass(Long classId, Long creatorId) {
        findClassByCreator(classId, creatorId);
        liveClassRepository.deleteById(classId);
    }

    public ClassListResponse getClassList(ClassStatus status) {
        List<LiveClass> classes = liveClassRepository.findAllByStatusDynamic(status);
        return new ClassListResponse(classes.stream().map(ClassSimpleResponse::from).toList());
    }

    public ClassDetailResponse getClassDetail(Long classId) {
        LiveClass liveClass = getClassOrThrow(classId);
        long currentCount = enrollmentRepository.countByLiveClassIdAndStatusNot(classId, EnrollmentStatus.CANCELLED);
        return ClassDetailResponse.from(liveClass, currentCount);
    }

    @Transactional
    public LiveClassResponse openClass(Long classId, Long creatorId) {
        LiveClass liveClass = findClassByCreator(classId, creatorId);
        liveClass.open();
        return LiveClassResponse.from(liveClass);
    }

    @Transactional
    public void closeExpiredClasses(LocalDate now) {
        List<LiveClass> expiredClasses = liveClassRepository.findByStatusAndEndDateBefore(ClassStatus.OPEN, now.plusDays(1));
        expiredClasses.forEach(LiveClass::close);
    }

    private LiveClass getClassOrThrow(Long classId) {
        return liveClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 존재하지 않습니다."));
    }

    private Member checkCreator(Long creatorId) {
        Member member = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
        if (member.getRole() != MemberRole.CREATOR) {
            throw new IllegalArgumentException("크리에이터가 아닙니다.");
        }
        return member;
    }

    private LiveClass findClassByCreator(Long classId, Long creatorId) {
        checkCreator(creatorId);
        LiveClass liveClass = getClassOrThrow(classId);
        if (!liveClass.getCreator().getId().equals(creatorId)) {
            throw new IllegalArgumentException("해당 강의의 크리에이터가 아닙니다.");
        }
        return liveClass;
    }
}
