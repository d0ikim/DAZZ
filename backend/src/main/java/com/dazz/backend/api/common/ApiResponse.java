package com.dazz.backend.api.common;

public record ApiResponse<T>(boolean success, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> fail(T error) {
        return new ApiResponse<>(false, error);
    }
}
