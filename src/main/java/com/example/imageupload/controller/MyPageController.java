package com.example.imageupload.controller;

import com.example.imageupload.dto.response.ResponseDto;
import com.example.imageupload.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("api/auth/mypage")
    public ResponseDto<?> getMypage(HttpServletRequest request){
        return myPageService.getMyPage(request);
    }


}
