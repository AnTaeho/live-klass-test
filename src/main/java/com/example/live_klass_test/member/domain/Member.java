package com.example.live_klass_test.member.domain;

import com.example.live_klass_test.common.domain.BaseEntity;
import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<LiveClass> classes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

//    @OneToMany(mappedBy = "member")
//    private List<WaitList> waitLists = new ArrayList<>();

    public Member(String name, String email, MemberRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public void add(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }
}
