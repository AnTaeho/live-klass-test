package com.example.live_klass_test.enrollment.domain;

import com.example.live_klass_test.common.domain.BaseEntity;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_class_id")
    private LiveClass liveClass;

    public Enrollment(Member member, LiveClass liveClass) {
        this.status = EnrollmentStatus.PENDING;
        this.member = member;
        member.add(this);
        this.liveClass = liveClass;
        liveClass.add(this);
    }

    public void completePay() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("결제는 PENDING 상태에서만 가능합니다.");
        }
        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 수강 신청입니다.");
        }
        if (this.status == EnrollmentStatus.CONFIRMED) {
            if (confirmedAt.plusDays(7).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("취소 가능 기간(" + 7 + "일)이 지났습니다.");
            }
        }
        this.status = EnrollmentStatus.CANCELLED;
    }

    public boolean isConfirmed() {
        return this.status == EnrollmentStatus.CONFIRMED;
    }

    public void expireBySystem() {
        if (this.status == EnrollmentStatus.PENDING) {
            this.status = EnrollmentStatus.CANCELLED;
        }
    }

}

