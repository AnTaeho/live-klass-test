package com.example.live_klass_test.liveclass.scheduler;

import com.example.live_klass_test.liveclass.service.LiveClassService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveClassScheduler {

    private final LiveClassService liveClassService;

    @Scheduled(cron = "0 0 0 * * *")
    public void closeExpiredClasses() {
        liveClassService.closeExpiredClasses(LocalDate.now());
    }
}
