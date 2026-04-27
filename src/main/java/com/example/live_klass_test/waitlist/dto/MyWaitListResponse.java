package com.example.live_klass_test.waitlist.dto;

import com.example.live_klass_test.waitlist.domain.WaitList;
import com.example.live_klass_test.waitlist.domain.WaitListStatus;
import java.time.LocalDateTime;

public record MyWaitListResponse(
        Long waitListId,
        String classTitle,
        int position,
        WaitListStatus status,
        LocalDateTime promotedAt
) {
    public static MyWaitListResponse from(WaitList waitList) {
        return new MyWaitListResponse(
                waitList.getId(),
                waitList.getLiveClass().getTitle(),
                waitList.getPosition(),
                waitList.getStatus(),
                waitList.getPromotedAt()
        );
    }
}
