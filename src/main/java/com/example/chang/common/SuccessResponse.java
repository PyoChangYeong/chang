package com.example.chang.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SuccessResponse {
    private int status;
    private String message;

    @Builder
    private SuccessResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static SuccessResponse of(HttpStatus status, String message){
        return SuccessResponse.builder()
                .status(status.value())
                .message(message)
                .build();

    }

}