package com.example.live_klass_test.member.dto;

import com.example.live_klass_test.member.domain.Member;

public record MemberLoginResponse(Long id, String name, String role) {
    public static MemberLoginResponse from(Member member) {
        return new MemberLoginResponse(
                member.getId(),
                member.getName(),
                member.getRole().name().toLowerCase()
        );
    }
}
