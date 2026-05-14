package com.smartparking.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Mã OTP không được để trống")
    private String otpCode;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}