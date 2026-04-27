package com.example.live_klass_test.waitlist.domain;

import com.example.live_klass_test.common.domain.BaseEntity;
import com.example.live_klass_test.enrollment.domain.Enrollment;
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
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaitList extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wait_list_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_class_id")
    private LiveClass liveClass;

    private int position;

    @Enumerated(EnumType.STRING)
    private WaitListStatus status;

    private LocalDateTime promotedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    public WaitList(Member member, LiveClass liveClass, int position) {
        this.member = member;
        this.liveClass = liveClass;
        this.position = position;
        this.status = WaitListStatus.WAITING;
    }

    public void promote(Enrollment enrollment) {
        this.status = WaitListStatus.PROMOTED;
        this.promotedAt = LocalDateTime.now();
        this.enrollment = enrollment;
    }

    public void cancel() {
        if (this.status == WaitListStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 대기열입니다.");
        }
        if (this.status == WaitListStatus.PROMOTED) {
            throw new IllegalStateException("이미 수강 신청으로 전환된 대기열은 취소할 수 없습니다.");
        }
        this.status = WaitListStatus.CANCELLED;
    }

    public void expirePromotion() {
        this.status = WaitListStatus.CANCELLED;
    }
}
