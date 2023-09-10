package com.example.chang.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {

    private final Error error;

    @Override
    public String getMessage() {
        return error.getMessage();
    }
}