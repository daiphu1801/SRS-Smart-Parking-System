package com.smartparking.shared.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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


    public Map<String, Object> loginWithPassword(String username, String password) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";
        Map<String, String> body;

        if (username.startsWith("0")) {
            username = "84" + username.substring(1);
        }else if( username.startsWith("+")){
            username = username.substring(1);
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


    public String createAdminUser(String phone, String password, Map<String, Object> appMetadata) {
        String url = supabaseUrl + "/auth/v1/admin/users";

// Khởi tạo HashMap có thể chỉnh sửa được
        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", phone);
        payload.put("password", password);
        payload.put("phone_confirm", true);
        payload.put("app_metadata", appMetadata);

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

    public void updateUserMetadata(String supabaseId, Map<String, Object> appMetadata) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + supabaseId;

        Map<String, Object> payload = Map.of("app_metadata", appMetadata);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, createAdminHeaders());

        try {

            restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
            log.info("Đã đồng bộ metadata lên Supabase cho user: {}", supabaseId);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Supabase: {}", e.getMessage());
        }
    }

    public void updateUserPassword(String supabaseUid, String newPassword) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + supabaseUid;

        // Body chứa mật khẩu mới
        Map<String, Object> body = new HashMap<>();
        body.put("password", newPassword);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, createAdminHeaders());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Lỗi từ Supabase khi update password: {}", response.getBody());
                throw new RuntimeException("Không thể cập nhật mật khẩu trên Supabase");
            }

            log.info("Cập nhật mật khẩu Supabase thành công cho UID: {}", supabaseUid);

        } catch (HttpClientErrorException e) {
            log.error("HTTP Error khi gọi API Supabase Admin: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi từ máy chủ xác thực: " + e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gọi Supabase Admin: {}", e.getMessage());
            throw new RuntimeException("Lỗi hệ thống khi cập nhật mật khẩu.");
        }
    }

}
