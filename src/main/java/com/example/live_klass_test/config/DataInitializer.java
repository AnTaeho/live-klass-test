package com.example.live_klass_test.config;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final LiveClassRepository liveClassRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final List<String> CLASS_TITLES = List.of(
            "스프링 부트로 배우는 백엔드 개발",
            "React와 TypeScript 실전 프로젝트",
            "데이터베이스 설계와 SQL 최적화",
            "클라우드 인프라와 CI/CD 파이프라인",
            "알고리즘과 자료구조 완전 정복"
    );

    private static final List<String> CLASS_DESCS = List.of(
            "Spring Boot를 활용한 REST API 설계부터 배포까지 다룹니다.",
            "React와 TypeScript를 이용한 실전 웹 애플리케이션을 개발합니다.",
            "관계형 DB 설계 원칙과 쿼리 성능 최적화 기법을 배웁니다.",
            "AWS 기반 인프라 구성과 Github Actions CI/CD를 실습합니다.",
            "코딩 테스트에 자주 나오는 알고리즘과 자료구조를 정리합니다."
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.count() > 0) {
            return;
        }

        List<Member> creators = List.of(
                new Member("강사1", "creator1@test.com", MemberRole.CREATOR),
                new Member("강사2", "creator2@test.com", MemberRole.CREATOR),
                new Member("강사3", "creator3@test.com", MemberRole.CREATOR),
                new Member("강사4", "creator4@test.com", MemberRole.CREATOR),
                new Member("강사5", "creator5@test.com", MemberRole.CREATOR)
        );
        memberRepository.saveAll(creators);

        List<Member> students = List.of(
                new Member("학생1", "student1@test.com", MemberRole.STUDENT),
                new Member("학생2", "student2@test.com", MemberRole.STUDENT),
                new Member("학생3", "student3@test.com", MemberRole.STUDENT),
                new Member("학생4", "student4@test.com", MemberRole.STUDENT),
                new Member("학생5", "student5@test.com", MemberRole.STUDENT),
                new Member("학생6", "student6@test.com", MemberRole.STUDENT),
                new Member("학생7", "student7@test.com", MemberRole.STUDENT),
                new Member("학생8", "student8@test.com", MemberRole.STUDENT),
                new Member("학생9", "student9@test.com", MemberRole.STUDENT),
                new Member("학생10", "student10@test.com", MemberRole.STUDENT)
        );
        memberRepository.saveAll(students);

        LocalDate today = LocalDate.now();
        // 인덱스 0, 1번 강의는 만석(maxCapacity=10, 학생 10명 전원 등록)
        int[] capacities = {10, 10, 20, 20, 20};
        boolean[] isFull = {true, true, false, false, false};

        for (int i = 0; i < creators.size(); i++) {
            LiveClass liveClass = new LiveClass(
                    CLASS_TITLES.get(i),
                    CLASS_DESCS.get(i),
                    50000 * (i + 1),
                    capacities[i],
                    today,
                    today.plusMonths(3),
                    creators.get(i)
            );
            liveClass.open();
            liveClassRepository.save(liveClass);

            if (isFull[i]) {
                for (Member student : students) {
                    Enrollment enrollment = new Enrollment(student, liveClass);
                    enrollment.completePay();
                    enrollmentRepository.save(enrollment);
                }
            }
        }
    }
}
