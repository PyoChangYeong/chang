package com.example.chang.common;

import com.example.chang.common.exceptions.ErrorResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDto<T> {

    private boolean success;
    private T response;

    private ErrorResponse error;

    @Builder
    private ApiResponseDto(boolean success, T response,ErrorResponse error) {
        this.success = success;
        this.response = response;
        this.error = error;
    }

}
