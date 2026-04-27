package com.example.live_klass_test.waitlist.controller;

import com.example.live_klass_test.common.dto.CommonResponse;
import com.example.live_klass_test.common.dto.EmptyDto;
import com.example.live_klass_test.waitlist.dto.MyWaitListResponse;
import com.example.live_klass_test.waitlist.service.WaitListService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/waitlists")
public class WaitListController {

    private final WaitListService waitListService;

    @GetMapping("/my/{memberId}")
    public CommonResponse<List<MyWaitListResponse>> getMyWaitList(@PathVariable Long memberId) {
        return new CommonResponse<>(waitListService.getMyWaitList(memberId));
    }

    @DeleteMapping("/{waitListId}")
    public CommonResponse<EmptyDto> cancelWaitList(
            @PathVariable Long waitListId,
            @RequestParam Long memberId) {
        waitListService.cancelWaitList(waitListId, memberId);
        return CommonResponse.EMPTY;
    }
}
