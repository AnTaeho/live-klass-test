package com.example.live_klass_test.member;

import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.dto.MemberJoinRequest;
import com.example.live_klass_test.member.dto.MemberResponse;
import com.example.live_klass_test.member.repository.MemberRepository;
import com.example.live_klass_test.member.service.MemberService;
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
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Test
    @DisplayName("학생 회원가입 성공")
    void joinStudent_success() {
        MemberJoinRequest request = new MemberJoinRequest("테스트학생", "student_test@example.com");

        MemberResponse response = memberService.join(request);

        Member saved = memberRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(MemberRole.STUDENT);
        assertThat(saved.getEmail()).isEqualTo("student_test@example.com");
    }

    @Test
    @DisplayName("크리에이터 회원가입 성공")
    void joinCreator_success() {
        MemberJoinRequest request = new MemberJoinRequest("테스트강사", "creator_test@example.com");

        MemberResponse response = memberService.joinCreator(request);

        Member saved = memberRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(MemberRole.CREATOR);
        assertThat(saved.getName()).isEqualTo("테스트강사");
    }

    @Test
    @DisplayName("중복 이메일로 학생 가입 시 예외 발생")
    void joinStudent_duplicateEmail_throws() {
        memberService.join(new MemberJoinRequest("학생A", "dup@example.com"));

        assertThatThrownBy(() -> memberService.join(new MemberJoinRequest("학생B", "dup@example.com")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    @DisplayName("중복 이메일로 크리에이터 가입 시 예외 발생")
    void joinCreator_duplicateEmail_throws() {
        memberService.joinCreator(new MemberJoinRequest("강사A", "dup_creator@example.com"));

        assertThatThrownBy(() -> memberService.joinCreator(new MemberJoinRequest("강사B", "dup_creator@example.com")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    @DisplayName("학생과 크리에이터가 같은 이메일 사용 불가")
    void joinCreatorAndStudent_sameEmail_throws() {
        memberService.join(new MemberJoinRequest("학생A", "shared@example.com"));

        assertThatThrownBy(() -> memberService.joinCreator(new MemberJoinRequest("강사A", "shared@example.com")))
                .isInstanceOf(IllegalStateException.class);
    }
}
