package com.example.live_klass_test.liveclass.repository;

import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface LiveClassRepository extends JpaRepository<LiveClass, Long>, LiveClassRepositoryCustom {
    List<LiveClass> findByStatusAndEndDateBefore(ClassStatus status, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lc FROM LiveClass lc WHERE lc.id = :id")
    Optional<LiveClass> findByIdWithLock(Long id);
}
