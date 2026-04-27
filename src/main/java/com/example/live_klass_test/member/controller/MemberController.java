package com.example.live_klass_test.member.controller;

import com.example.live_klass_test.common.dto.CommonResponse;
import com.example.live_klass_test.member.dto.MemberJoinRequest;
import com.example.live_klass_test.member.dto.MemberLoginRequest;
import com.example.live_klass_test.member.dto.MemberLoginResponse;
import com.example.live_klass_test.member.dto.MemberResponse;
import com.example.live_klass_test.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<MemberResponse> join(@RequestBody @Valid MemberJoinRequest memberJoinRequest) {
        return new CommonResponse<>(memberService.join(memberJoinRequest));
    }

    @PostMapping("/creator")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<MemberResponse> joinCreator(@RequestBody @Valid MemberJoinRequest memberJoinRequest) {
        return new CommonResponse<>(memberService.joinCreator(memberJoinRequest));
    }

    @PostMapping("/login")
    public CommonResponse<MemberLoginResponse> login(@RequestBody @Valid MemberLoginRequest memberLoginRequest) {
        return new CommonResponse<>(memberService.login(memberLoginRequest));
    }
}
