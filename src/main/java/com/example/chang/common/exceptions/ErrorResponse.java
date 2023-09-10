package com.example.chang.common.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private String status;
    private String message;

    @Builder
    private ErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ErrorResponse of(String status, String message){
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .build();
    }

    public static ErrorResponse of(Error error) {
        return ErrorResponse.builder()
                .status(error.getStatus())
                .message(error.getMessage())
                .build();
    }
}