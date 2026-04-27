package com.example.live_klass_test.payment.domain;

import com.example.live_klass_test.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private Long memberId;
    private Long liveClassId;
    private Long enrollmentId;
    private int price;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public Payment(Long memberId, Long liveClassId, Long enrollmentId, int price) {
        this.memberId = memberId;
        this.liveClassId = liveClassId;
        this.enrollmentId = enrollmentId;
        this.price = price;
        this.status = PaymentStatus.PAID;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}
