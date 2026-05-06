package com.smartparking.identity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("phone"), body.get("otp_code"), body.get("new_password"));
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        String token = authHeader;
        if (authHeader.toLowerCase().startsWith("bearer ")) {
            token = authHeader.substring(7); // Cắt 7 ký tự đầu tiên ("Bearer ")
        }
        authService.changePassword(token, body.get("new_password"));
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@RequestAttribute("accountId") Integer accountId) {
        ProfileResponse data = authService.getProfile(accountId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<Void>> registerSendOtp(@RequestBody Map<String, String> body) {
        authService.registerSendOtp(body.get("phone"));
        return ResponseEntity.ok(ApiResponse.success("Mã xác nhận đã gửi"));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> registerVerify(@RequestBody Map<String, String> body) {
        AuthLoginResponse data = authService.registerVerify(body.get("phone"), body.get("otp_code"), body.get("password"));
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
