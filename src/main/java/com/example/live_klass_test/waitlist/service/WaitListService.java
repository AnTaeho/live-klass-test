package com.example.live_klass_test.waitlist.service;

import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.waitlist.domain.WaitList;
import com.example.live_klass_test.waitlist.domain.WaitListStatus;
import com.example.live_klass_test.waitlist.dto.MyWaitListResponse;
import com.example.live_klass_test.waitlist.dto.WaitListJoinResponse;
import com.example.live_klass_test.waitlist.repository.WaitListRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitListService {

    private static final int PROMOTION_EXPIRE_HOURS = 48;

    private final WaitListRepository waitListRepository;
    private final WaitListPromotionService waitListPromotionService;

    @Transactional
    public WaitListJoinResponse addToWaitList(Member member, LiveClass liveClass) {
        if (waitListRepository.existsByMemberIdAndLiveClassIdAndStatus(
                member.getId(), liveClass.getId(), WaitListStatus.WAITING)) {
            throw new IllegalStateException("이미 대기 중인 강의입니다.");
        }
        int nextPosition = waitListRepository.findMaxPositionByLiveClassId(liveClass.getId()) + 1;
        WaitList waitList = new WaitList(member, liveClass, nextPosition);
        waitListRepository.save(waitList);
        return new WaitListJoinResponse(waitList.getId(), nextPosition);
    }

    @Transactional
    public void expireOverduePromotions() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(PROMOTION_EXPIRE_HOURS);
        List<WaitList> expiredList = waitListRepository.findExpiredPromotions(expireTime);

        for (WaitList waitList : expiredList) {
            waitList.getEnrollment().expireBySystem();
            waitList.expirePromotion();
            waitListPromotionService.promoteNextInLine(waitList.getLiveClass().getId());
        }
    }

    @Transactional
    public void cancelWaitList(Long waitListId, Long memberId) {
        WaitList waitList = waitListRepository.findById(waitListId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대기 내역이 없습니다."));
        if (!waitList.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 대기열만 취소할 수 있습니다.");
        }
        waitList.cancel();
    }

    public List<MyWaitListResponse> getMyWaitList(Long memberId) {
        return waitListRepository.findByMemberIdWithClass(memberId).stream()
                .map(MyWaitListResponse::from)
                .toList();
    }
}
