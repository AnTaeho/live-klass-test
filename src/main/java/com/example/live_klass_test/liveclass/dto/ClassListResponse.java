package com.example.live_klass_test.liveclass.dto;

import java.util.List;

public record ClassListResponse(
        List<ClassSimpleResponse> classes
) {
}
