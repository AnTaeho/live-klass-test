package com.example.live_klass_test.liveclass;

import com.example.live_klass_test.enrollment.domain.Enrollment;
import com.example.live_klass_test.enrollment.repository.EnrollmentRepository;
import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.domain.LiveClass;
import com.example.live_klass_test.liveclass.dto.ClassDetailResponse;
import com.example.live_klass_test.liveclass.dto.ClassListResponse;
import com.example.live_klass_test.liveclass.dto.CreateClassRequest;
import com.example.live_klass_test.liveclass.dto.LiveClassResponse;
import com.example.live_klass_test.liveclass.repository.LiveClassRepository;
import com.example.live_klass_test.liveclass.service.LiveClassService;
import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.repository.MemberRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LiveClassServiceTest {

    @Autowired LiveClassService liveClassService;
    @Autowired LiveClassRepository liveClassRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired EnrollmentRepository enrollmentRepository;

    private Member creator;
    private Member student;

    @BeforeEach
    void setUp() {
        creator = memberRepository.save(new Member("강사", "lc_creator@example.com", MemberRole.CREATOR));
        student = memberRepository.save(new Member("학생", "lc_student@example.com", MemberRole.STUDENT));
    }

    private CreateClassRequest validRequest(int capacity) {
        return new CreateClassRequest(
                "테스트 강의",
                "강의 설명",
                50000,
                capacity,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(1)
        );
    }

    @Test
    @DisplayName("강의 생성 성공 - DRAFT 상태")
    void createClass_success() {
        LiveClassResponse response = liveClassService.createClass(validRequest(20), creator.getId());

        LiveClass saved = liveClassRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ClassStatus.DRAFT);
        assertThat(saved.getTitle()).isEqualTo("테스트 강의");
        assertThat(saved.getCreator().getId()).isEqualTo(creator.getId());
    }

    @Test
    @DisplayName("학생이 강의 생성 시도 시 예외")
    void createClass_asStudent_throws() {
        assertThatThrownBy(() -> liveClassService.createClass(validRequest(20), student.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("크리에이터가 아닙니다");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 강의 생성 시 예외")
    void createClass_unknownUser_throws() {
        assertThatThrownBy(() -> liveClassService.createClass(validRequest(20), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 사용자가 없습니다");
    }

    @Test
    @DisplayName("강의 삭제 성공")
    void deleteClass_success() {
        LiveClassResponse created = liveClassService.createClass(validRequest(10), creator.getId());

        liveClassService.deleteClass(created.id(), creator.getId());

        assertThat(liveClassRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("다른 크리에이터가 강의 삭제 시 예외")
    void deleteClass_wrongCreator_throws() {
        Member otherCreator = memberRepository.save(new Member("다른강사", "other_creator@example.com", MemberRole.CREATOR));
        LiveClassResponse created = liveClassService.createClass(validRequest(10), creator.getId());

        assertThatThrownBy(() -> liveClassService.deleteClass(created.id(), otherCreator.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("강의 목록 조회 - 필터 없음")
    void getClassList_noFilter() {
        liveClassService.createClass(validRequest(10), creator.getId());
        liveClassService.createClass(validRequest(5), creator.getId());

        ClassListResponse response = liveClassService.getClassList(null);

        assertThat(response.classes().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("강의 목록 조회 - DRAFT 필터")
    void getClassList_draftFilter() {
        LiveClassResponse created = liveClassService.createClass(validRequest(10), creator.getId());
        liveClassService.openClass(created.id(), creator.getId());

        LiveClassResponse draftClass = liveClassService.createClass(validRequest(5), creator.getId());

        ClassListResponse draftList = liveClassService.getClassList(ClassStatus.DRAFT);
        ClassListResponse openList = liveClassService.getClassList(ClassStatus.OPEN);

        assertThat(draftList.classes()).allMatch(c -> c.status() == ClassStatus.DRAFT);
        assertThat(openList.classes()).allMatch(c -> c.status() == ClassStatus.OPEN);
    }

    @Test
    @DisplayName("강의 상세 조회 - 수강생 수 포함")
    void getClassDetail_includesEnrollmentCount() {
        LiveClassResponse created = liveClassService.createClass(validRequest(10), creator.getId());
        liveClassService.openClass(created.id(), creator.getId());
        LiveClass liveClass = liveClassRepository.findById(created.id()).orElseThrow();
        enrollmentRepository.save(new Enrollment(student, liveClass));

        ClassDetailResponse detail = liveClassService.getClassDetail(created.id());

        assertThat(detail.currentEnrollmentCount()).isEqualTo(1);
        assertThat(detail.title()).isEqualTo("테스트 강의");
    }

    @Test
    @DisplayName("강의 OPEN 성공")
    void openClass_success() {
        LiveClassResponse created = liveClassService.createClass(validRequest(10), creator.getId());

        liveClassService.openClass(created.id(), creator.getId());

        LiveClass liveClass = liveClassRepository.findById(created.id()).orElseThrow();
        assertThat(liveClass.getStatus()).isEqualTo(ClassStatus.OPEN);
    }

    @Test
    @DisplayName("DRAFT가 아닌 강의를 OPEN 시도 시 예외")
    void openClass_notDraft_throws() {
        LiveClassResponse created = liveClassService.createClass(validRequest(10), creator.getId());
        liveClassService.openClass(created.id(), creator.getId());

        assertThatThrownBy(() -> liveClassService.openClass(created.id(), creator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT 상태에서만");
    }

    @Test
    @DisplayName("만료된 강의 자동 종료")
    void closeExpiredClasses_closesOldOpenClasses() {
        LiveClass expiredClass = new LiveClass(
                "만료 강의", "설명", 10000, 5,
                LocalDate.now().minusMonths(2),
                LocalDate.now().minusDays(1),
                creator
        );
        expiredClass.open();
        liveClassRepository.save(expiredClass);

        liveClassService.closeExpiredClasses(LocalDate.now());

        LiveClass found = liveClassRepository.findById(expiredClass.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(ClassStatus.CLOSED);
    }
}
