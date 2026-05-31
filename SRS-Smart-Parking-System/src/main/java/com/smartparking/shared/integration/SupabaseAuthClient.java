package com.smartparking.shared.integration;

import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupabaseAuthClient {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;


    private final RestTemplate restTemplate;

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
    private HttpHeaders createAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseServiceRoleKey);
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
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
        } else {
            body = Map.of("email", username, "password", password);
        }

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, createPublicHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.warn("Login thất bại (Sai thông tin) từ user: {}", maskUsername(username));
            throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác!");

        } catch (Exception e) {
            log.error("CRITICAL: Lỗi kết nối Supabase Auth System: {}", e.getMessage());
            throw new RuntimeException("Hệ thống đăng nhập đang bảo trì. Vui lòng thử lại sau!");
        }
    }

    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void logout(String userToken) {
        String url = supabaseUrl + "/auth/v1/logout";
        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of(), createUserHeaders(userToken));

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException e) {
            log.warn("User logout với token đã hết hạn/không hợp lệ.");
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi gọi Supabase Logout: {}", e.getMessage());
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
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return (String) response.getBody().get("id");

        } catch (HttpClientErrorException e) {
            log.warn("Tạo user thất bại (Lỗi Data): {}", e.getResponseBodyAsString());
            throw new BusinessException("Không thể tạo người dùng: Số điện thoại đã tồn tại hoặc dữ liệu không hợp lệ.");

        } catch (Exception e) {
            log.error("CRITICAL: Lỗi khi tạo user trên Supabase Admin API: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo tài khoản bảo mật. Vui lòng thử lại sau.");
        }
    }

    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void updateUserMetadata(String supabaseId, Map<String, Object> appMetadata) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + supabaseId;

        Map<String, Object> payload = Map.of("app_metadata", appMetadata);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, createAdminHeaders());

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
        } catch (Exception e) {
            log.error("CRITICAL: Lỗi đồng bộ Metadata lên Supabase cho ID {}: {}", supabaseId, e.getMessage());
            throw new RuntimeException("Lỗi đồng bộ phân quyền lên hệ thống bảo mật!");
        }
    }

    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
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
    private String maskUsername(String username) {
        if (username == null || username.length() < 6) return "***";
        return username.substring(0, 3) + "***" + username.substring(username.length() - 4);
    }


}
