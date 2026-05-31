package com.smartparking.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // Dùng số nguyên (int) cho chuẩn RESTful API (200, 400, 404, 500...)
    private int code;
    private String message;
    private T data;

    // ==========================================
    // CÁC HÀM XỬ LÝ THÀNH CÔNG (Mặc định code 200)
    // ==========================================

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Thành công")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .code(200)
                .message(message)
                .build();
    }

    // ==========================================
    // HÀM XỬ LÝ LỖI CHO GLOBAL EXCEPTION HANDLER
    // ==========================================

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null) // Lỗi thì không có data trả về
                .build();
    }
}