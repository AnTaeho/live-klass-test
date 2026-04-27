package com.example.live_klass_test.member.service;

import com.example.live_klass_test.member.domain.Member;
import com.example.live_klass_test.member.domain.MemberRole;
import com.example.live_klass_test.member.dto.MemberJoinRequest;
import com.example.live_klass_test.member.dto.MemberLoginRequest;
import com.example.live_klass_test.member.dto.MemberLoginResponse;
import com.example.live_klass_test.member.dto.MemberResponse;
import com.example.live_klass_test.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        return createMember(request, MemberRole.STUDENT);
    }

    @Transactional
    public MemberResponse joinCreator(MemberJoinRequest request) {
        return createMember(request, MemberRole.CREATOR);
    }

    public MemberLoginResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        return MemberLoginResponse.from(member);
    }

    private MemberResponse createMember(MemberJoinRequest request, MemberRole role) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        Member savedMember = memberRepository.save(new Member(request.name(), request.email(), role));
        return MemberResponse.from(savedMember);
    }
}
