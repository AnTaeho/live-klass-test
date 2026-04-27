package com.example.live_klass_test.liveclass.repository;

import static com.example.live_klass_test.liveclass.domain.QLiveClass.liveClass;

import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LiveClassRepositoryCustomImpl implements LiveClassRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LiveClass> findAllByStatusDynamic(ClassStatus status) {
        return queryFactory
                .selectFrom(liveClass)
                .where(statusEq(status))
                .fetch();
    }

    private BooleanExpression statusEq(ClassStatus status) {
        return status != null ? liveClass.status.eq(status) : null;
    }
}
