package com.example.live_klass_test.waitlist.scheduler;

import com.example.live_klass_test.waitlist.service.WaitListService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitListScheduler {

    private final WaitListService waitListService;

    @Scheduled(cron = "0 0 * * * *")
    public void expireOverduePromotions() {
        waitListService.expireOverduePromotions();
    }
}
