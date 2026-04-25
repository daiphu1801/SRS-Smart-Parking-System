package com.smartparking.controller;

import com.smartparking.service.AuthResponse;
import com.smartparking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/request")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> body) {
        authService.sendOtp(body.get("phone"));
        return ResponseEntity.ok(Map.of("message", "OTP sent to " + body.get("phone")));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        AuthResponse response = authService.verifyOtpAndLogin(body.get("phone"), body.get("otp"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/employee/login")
    public ResponseEntity<?> employeeLogin(@RequestBody Map<String, String> body) {
        AuthResponse response = authService.employeeLogin(body.get("username"), body.get("password"));
        return ResponseEntity.ok(response);
    }
}
