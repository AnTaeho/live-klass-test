package com.example.live_klass_test.waitlist.repository;

import com.example.live_klass_test.waitlist.domain.WaitList;
import com.example.live_klass_test.waitlist.domain.WaitListStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WaitListRepository extends JpaRepository<WaitList, Long> {

    Optional<WaitList> findFirstByLiveClassIdAndStatusOrderByPositionAsc(Long liveClassId, WaitListStatus status);

    boolean existsByMemberIdAndLiveClassIdAndStatus(Long memberId, Long liveClassId, WaitListStatus status);

    @Query("SELECT w FROM WaitList w JOIN FETCH w.liveClass WHERE w.member.id = :memberId ORDER BY w.createdAt DESC")
    List<WaitList> findByMemberIdWithClass(Long memberId);

    @Query("SELECT COALESCE(MAX(w.position), 0) FROM WaitList w WHERE w.liveClass.id = :liveClassId")
    int findMaxPositionByLiveClassId(Long liveClassId);

    @Query("SELECT w FROM WaitList w JOIN FETCH w.enrollment JOIN FETCH w.liveClass " +
           "WHERE w.status = 'PROMOTED' AND w.promotedAt < :expireTime")
    List<WaitList> findExpiredPromotions(LocalDateTime expireTime);
}
