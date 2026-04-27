package com.example.live_klass_test.liveclass.controller;

import com.example.live_klass_test.common.dto.CommonResponse;
import com.example.live_klass_test.common.dto.EmptyDto;
import com.example.live_klass_test.liveclass.domain.ClassStatus;
import com.example.live_klass_test.liveclass.dto.ClassDetailResponse;
import com.example.live_klass_test.liveclass.dto.ClassListResponse;
import com.example.live_klass_test.liveclass.dto.CreateClassRequest;
import com.example.live_klass_test.liveclass.dto.LiveClassResponse;
import com.example.live_klass_test.liveclass.service.LiveClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class LiveClassController {

    private final LiveClassService liveClassService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<LiveClassResponse> createClass(
            @RequestBody @Valid CreateClassRequest request,
            @RequestParam Long creatorId) {
        return new CommonResponse<>(liveClassService.createClass(request, creatorId));
    }

    @DeleteMapping("/{classId}")
    public CommonResponse<EmptyDto> deleteClass(
            @PathVariable Long classId,
            @RequestParam Long creatorId) {
        liveClassService.deleteClass(classId, creatorId);
        return CommonResponse.EMPTY;
    }

    @GetMapping
    public CommonResponse<ClassListResponse> getClassList(
            @RequestParam(required = false) ClassStatus status) {
        return new CommonResponse<>(liveClassService.getClassList(status));
    }

    @GetMapping("/{classId}")
    public CommonResponse<ClassDetailResponse> getClassDetail(@PathVariable Long classId) {
        return new CommonResponse<>(liveClassService.getClassDetail(classId));
    }

    @PatchMapping("/{classId}/open")
    public CommonResponse<LiveClassResponse> openClass(
            @PathVariable Long classId,
            @RequestParam Long creatorId) {
        return new CommonResponse<>(liveClassService.openClass(classId, creatorId));
    }
}
