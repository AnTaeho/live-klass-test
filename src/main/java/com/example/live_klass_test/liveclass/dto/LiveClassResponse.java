package com.example.live_klass_test.liveclass.dto;

import com.example.live_klass_test.liveclass.domain.LiveClass;

public record LiveClassResponse(Long id) {
    public static LiveClassResponse from(LiveClass liveClass) {
        return new LiveClassResponse(liveClass.getId());
    }
}
