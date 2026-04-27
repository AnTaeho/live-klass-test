package com.example.live_klass_test.liveclass.repository;

import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import java.util.List;

public interface LiveClassRepositoryCustom {
    List<LiveClass> findAllByStatusDynamic(ClassStatus status);
}
