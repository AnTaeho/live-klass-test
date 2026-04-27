package com.example.live_klass_test.payment.repository;

import com.example.live_klass_test.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMemberIdAndEnrollmentId(Long memberId, Long enrollmentId);

    boolean existsByEnrollmentId(Long enrollmentId);

}
