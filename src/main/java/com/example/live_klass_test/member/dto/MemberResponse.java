package com.example.live_klass_test.member.dto;

import com.example.live_klass_test.member.domain.Member;

public record MemberResponse(Long id) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId());
    }
}
