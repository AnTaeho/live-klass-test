package com.example.live_klass_test.enrollment.repository;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.domain.EnrollmentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByLiveClassIdAndStatusNot(Long liveClassId, EnrollmentStatus status);

    List<Enrollment> findByMemberId(Long memberId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.liveClass WHERE e.member.id = :memberId")
    Page<Enrollment> findByMemberIdWithClass(Long memberId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.member WHERE e.liveClass.id = :liveClassId")
    Page<Enrollment> findByLiveClassIdWithMember(Long liveClassId, Pageable pageable);

    boolean existsByMemberIdAndLiveClassIdAndStatusNot(Long memberId, Long liveClassId, EnrollmentStatus status);
}
