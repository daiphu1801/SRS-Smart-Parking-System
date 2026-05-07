package com.smartparking.shared.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class SupabaseAuthClient {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;


    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders createPublicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", anonKey);
        headers.set("Authorization", "Bearer " + anonKey);
        return headers;
    }

    private HttpHeaders createUserHeaders(String userToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", anonKey);

        if (StringUtils.hasText(userToken)) {
            String authHeader = userToken.startsWith("Bearer ") ? userToken : "Bearer " + userToken;
            headers.set("Authorization", authHeader);
        }
        return headers;
    }
    // Hàm tạo Header xài chung cho các tác vụ cần quyền Admin
    private HttpHeaders createAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseServiceRoleKey);
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

//    public void sendOtp(String phone) {
//        String url = supabaseUrl + "/auth/v1/otp";
//        Map<String, String> body = Map.of("phone", phone);
//
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, createPublicHeaders());
//
//        try {
//            restTemplate.postForEntity(url, request, String.class);
//            log.info("Requested Supabase to send OTP to {}", phone);
//        } catch (Exception e) {
//            log.error("Failed to send OTP via Supabase: {}", e.getMessage());
//            throw new RuntimeException("Failed to send OTP", e);
//        }
//    }

//    public Map<String, Object> verifyOtp(String phone, String otp) {
//        String url = supabaseUrl + "/auth/v1/verify";
//        Map<String, String> body = Map.of(
//                "phone", phone,
//                "token", otp,
//                "type", "sms");
//
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, createPublicHeaders());
//
//        try {
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
//            return response.getBody();
//        } catch (Exception e) {
//            log.error("Failed to verify OTP via Supabase: {}", e.getMessage());
//            throw new IllegalArgumentException("Invalid OTP or Supabase Error");
//        }
//    }

    public Map<String, Object> loginWithPassword(String username, String password) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";
        Map<String, String> body;

        if (username.startsWith("0")) {
            username = "84" + username.substring(1);
        }

        if (username.matches("^\\+?\\d+$")) {
            body = Map.of("phone", username, "password", password);
            log.info("Sending login request with PHONE: {}", username);
        } else {
            body = Map.of("email", username, "password", password);
            log.info("Sending login request with EMAIL: {}", username);
        }

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, createPublicHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to login via Supabase: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    // 1. Hàm Đăng xuất
    public void logout(String userToken) {
        String url = supabaseUrl + "/auth/v1/logout";

        // Truyền Header User (kèm token của họ) và body rỗng
        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of(), createUserHeaders(userToken));

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("Successfully logged out user on Supabase");
        } catch (Exception e) {
            log.error("Failed to logout via Supabase: {}", e.getMessage());
            throw new RuntimeException("Lỗi hệ thống khi đăng xuất", e);
        }
    }

    public void updatePassword(String sessionToken, String newPassword) {
        String url = supabaseUrl + "/auth/v1/user";

        // Truyền mật khẩu mới vào Body
        Map<String, String> body = Map.of("password", newPassword);

        // Truyền Header User (kèm token của họ)
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, createUserHeaders(sessionToken));

        try {
            // Lưu ý: Update API của Supabase dùng method PUT
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            log.info("Successfully updated password on Supabase");
        } catch (Exception e) {
            log.error("Failed to update password via Supabase: {}", e.getMessage());
            throw new RuntimeException("Cập nhật mật khẩu thất bại. Token có thể đã hết hạn.", e);
        }
    }

    public String createAdminUser(String phone, String password) {
        String url = supabaseUrl + "/auth/v1/admin/users";

        Map<String, Object> payload = Map.of(
                "phone", phone,
                "password", password,
                "phone_confirm", true
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, createAdminHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);
            return (String) response.getBody().get("id"); // Chỉ trả về đúng cái ID
        } catch (Exception e) {
            log.error("Lỗi khi tạo user trên Supabase Admin API: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo tài khoản bảo mật. Vui lòng thử lại sau.");
        }
    }


}
