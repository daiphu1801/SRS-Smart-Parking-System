package com.smartparking.identity.controller;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.identity.dto.request.ChangePasswordRequest;
import com.smartparking.identity.dto.request.ResetPasswordRequest;
import com.smartparking.identity.dto.request.SendOtpRequest;
import com.smartparking.identity.dto.request.VerifyOtpRequest;
import com.smartparking.identity.dto.response.CheckPhoneResponse;
import com.smartparking.identity.dto.response.ForgotPasswordResponse;
import com.smartparking.identity.entity.OtpType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.smartparking.identity.dto.response.AuthLoginResponse;
import com.smartparking.identity.dto.response.ProfileResponse;
import com.smartparking.identity.service.AuthService;
import com.smartparking.shared.dto.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> login(@RequestBody Map<String, String> body) {
        AuthLoginResponse data = authService.login(body.get("username"), body.get("password"));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<ApiResponse<CheckPhoneResponse>> checkPhone(@PathVariable String phone) {

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        CheckPhoneResponse data = authService.checkPhone(phone.trim());

        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {

        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@RequestAttribute("accountId") Integer accountId) {
        ProfileResponse data = authService.getProfile(accountId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> createSupabaseAccount(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String otpCode = body.get("otp_code"); // Tạm thời có thể null hoặc không dùng
        String password = body.get("password");
        if (phone == null || password == null) {
            throw new IllegalArgumentException("Số điện thoại và mật khẩu không được để trống");
        }

        // Gọi Service tạo tài khoản và tự động đăng nhập
        AuthLoginResponse data = authService.createSupabaseAccount(phone, otpCode, password);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {

        authService.sendOtp(request.getPhone(), request.getType());

        return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi thành công!"));
    }

    @PostMapping("/forgot-password/{phone}")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(@PathVariable String phone) {

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại để lấy lại mật khẩu");
        }

        ForgotPasswordResponse data= authService.sendOtp(phone.trim(), OtpType.FORGOT_PASSWORD);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AuthLoginResponse data = authService.resetPassword(request.getPhone(), request.getOtpCode(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> changePassword(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        Integer accountId = principal.getAccountId();
        AuthLoginResponse data = authService.changePassword(
                accountId,
                request.getOldPassword(),
                request.getNewPassword()
        );

        // FE nhận được cục này thì đè cái Token cũ đi là xong!
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
