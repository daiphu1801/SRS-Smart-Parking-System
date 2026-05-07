package com.smartparking.identity.controller;

import com.smartparking.identity.dto.response.CheckPhoneResponse;
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

    @PostMapping("/check-phone")
    public ResponseEntity<ApiResponse<CheckPhoneResponse>> checkPhone(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");

        // Validate nhẹ nhàng
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        // Gọi Service
        CheckPhoneResponse data = authService.checkPhone(phone.trim());

        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {

        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("phone"));
        return ResponseEntity.ok(ApiResponse.success("Mã xác nhận đã gửi"));
    }

//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> body) {
//        authService.resetPassword(body.get("phone"), body.get("otp_code"), body.get("new_password"));
//        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công"));
//    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body) {
        String token = jwt.getTokenValue();

        authService.changePassword(token, body.get("new_password"));
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@RequestAttribute("accountId") Integer accountId) {
        ProfileResponse data = authService.getProfile(accountId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

//    @PostMapping("/register/send-otp")
//    public ResponseEntity<ApiResponse<Void>> registerSendOtp(@RequestBody Map<String, String> body) {
//        authService.registerSendOtp(body.get("phone"));
//        return ResponseEntity.ok(ApiResponse.success("Mã xác nhận đã gửi"));
//    }

//    @PostMapping("/register/verify")
//    public ResponseEntity<ApiResponse<AuthLoginResponse>> registerVerify(@RequestBody Map<String, String> body) {
//        AuthLoginResponse data = authService.registerVerify(body.get("phone"), body.get("otp_code"), body.get("password"));
//        return ResponseEntity.ok(ApiResponse.success(data));
//    }

    @PostMapping("/register/createSupabaseAccount")
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
}
