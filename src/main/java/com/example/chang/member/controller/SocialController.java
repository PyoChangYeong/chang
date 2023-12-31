package com.example.chang.member.controller;

import com.example.chang.common.ApiResponseDto;
import com.example.chang.member.dto.SocialUserInfoDto;
import com.example.chang.member.service.KakaoService;
import com.example.chang.member.service.NaverService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SocialController {

    private final KakaoService kakaoService;
    private final NaverService naverService;


//    카카오
    @GetMapping("/oauth/callback/kakao")
    public ApiResponseDto<SocialUserInfoDto> kakao(@RequestParam String code, HttpServletResponse response)throws JsonProcessingException{
        return kakaoService.kakaoLogin(code, response);
    }


//    네이버
    @GetMapping("/oauth/callback/naver")
    public ApiResponseDto<SocialUserInfoDto> naver(@RequestParam String code, HttpServletResponse response)throws JsonProcessingException{
        return naverService.naverLogin(code, response);
    }

}
