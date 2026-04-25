package com.smartparking.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Zalo ZNS (Zalo Notification Service)
 * Dùng để gửi OTP, nhắc nợ và xác nhận thanh toán.
 * Tài liệu: https://developers.zalo.me/docs/zns
 */
@Service
@Slf4j
public class ZaloZNSService {

    @Value("${app.zns.access-token}")
    private String accessToken;

    @Value("${app.zns.app-id}")
    private String appId;

    @Value("${app.zns.template-otp}")
    private String templateOtp;

    @Value("${app.zns.template-reminder}")
    private String templateReminder;

    @Value("${app.zns.template-confirm}")
    private String templateConfirm;

    private static final String ZNS_URL = "https://business.openapi.zalo.me/message/template";
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String phone, String otp) {
        Map<String, Object> payload = Map.of(
            "phone", normalizePhone(phone),
            "template_id", templateOtp,
            "template_data", Map.of("otp", otp)
        );
        send(payload);
        log.info("ZNS OTP sent to {}", phone);
    }

    public void sendSubscriptionConfirmation(String phone, String vehicleNo, String endDate) {
        Map<String, Object> payload = Map.of(
            "phone", normalizePhone(phone),
            "template_id", templateConfirm,
            "template_data", Map.of("bien_so", vehicleNo, "ngay_het_han", endDate)
        );
        send(payload);
    }

    public void sendRenewalReminder(String phone, String vehicleNo, int daysLeft) {
        Map<String, Object> payload = Map.of(
            "phone", normalizePhone(phone),
            "template_id", templateReminder,
            "template_data", Map.of("bien_so", vehicleNo, "so_ngay_con_lai", daysLeft)
        );
        send(payload);
    }

    private void send(Map<String, Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("access_token", accessToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(ZNS_URL, request, String.class);
        } catch (Exception e) {
            log.error("ZNS send failed: {}", e.getMessage());
            // Non-blocking — notification failure không ảnh hưởng luồng chính
        }
    }

    /** VN phone: 0xxx → 84xxx */
    private String normalizePhone(String phone) {
        if (phone.startsWith("0")) return "84" + phone.substring(1);
        return phone;
    }
}
