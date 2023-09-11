package com.example.chang.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SocialUserInfoDto {
    private String email;
    private String username;

    @Builder
    public SocialUserInfoDto(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public static SocialUserInfoDto of(String email, String username){
        return SocialUserInfoDto.builder()
                .email(email)
                .username(username)
                .build();
    }
}
