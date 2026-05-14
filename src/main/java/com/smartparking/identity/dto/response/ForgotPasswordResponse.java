package com.smartparking.identity.dto.response;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotPasswordResponse {
    private String phone;
    private String message;
    // Ông có thể thêm các trường như thời gian hết hạn của OTP nếu muốn Frontend đếm ngược
    private int expireInMinutes;
}