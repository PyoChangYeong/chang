package com.example.chang.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SuccessLoginResponse {
    private int status;
    private String message;
    private String nickname;

    @Builder
    private SuccessLoginResponse(int status, String message, String nickname) {
        this.status = status;
        this.message = message;
        this.nickname = nickname;
    }

    public static SuccessLoginResponse of(HttpStatus status, String message, String nickname){
        return SuccessLoginResponse.builder()
                .status(status.value())
                .message(message)
                .nickname(nickname)
                .build();

    }

}