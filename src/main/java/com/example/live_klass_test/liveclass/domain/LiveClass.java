package com.example.live_klass_test.liveclass.domain;

import com.example.live_klass_test.common.domain.BaseEntity;
import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.member.domain.Member;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveClass extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "live_class_id")
    private Long id;

    private String title;
    private String description;
    private int price;
    private int maxCapacity;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ClassStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member creator;

    @OneToMany(mappedBy = "liveClass", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    public LiveClass(String title, String description, int price, int maxCapacity, LocalDate startDate,
                     LocalDate endDate, Member creator) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ClassStatus.DRAFT;
        this.creator = creator;
    }

    public void open() {
        if (this.status != ClassStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 OPEN할 수 있습니다.");
        }
        this.status = ClassStatus.OPEN;
    }

    public void close() {
        if (this.status != ClassStatus.OPEN) {
            throw new IllegalStateException("OPEN 상태에서만 CLOSED할 수 있습니다.");
        }
        this.status = ClassStatus.CLOSED;
    }

    public void add(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }

    public boolean isOverCapacity(long currentStudentCount) {
        return currentStudentCount >= this.maxCapacity;
    }
}
