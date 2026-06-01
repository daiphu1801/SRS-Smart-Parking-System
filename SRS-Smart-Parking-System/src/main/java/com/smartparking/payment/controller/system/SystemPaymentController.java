package com.smartparking.payment.controller.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.payment.dto.request.payment.PaymentSessionRequest;
import com.smartparking.payment.dto.request.payment.SepayWebhookRequest;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.payment.service.SystemPaymentService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/payments")
@RequiredArgsConstructor
public class SystemPaymentController {

    private final SystemPaymentService systemPaymentService;

    private final ObjectMapper objectMapper;

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiateSessionPayment(
            @RequestBody PaymentSessionRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Khởi tạo thanh toán lượt đỗ xe thành công",
                systemPaymentService.initiateSessionPayment(request)));
    }

    // --- 3. WEBHOOK TỪ NGÂN HÀNG TRẢ VỀ (Server to Server) ---
    @PostMapping("/webhook/sepay")
    public ResponseEntity<String> sepayWebhook(
            @RequestHeader(value = "X-SePay-Signature", required = false) String signature,
            @RequestHeader(value = "X-SePay-Timestamp", required = false) String timestamp,
            HttpServletRequest request) {

        if (signature == null || timestamp == null || signature.trim().isEmpty()) {
            return ResponseEntity.status(401).body("Thiếu chữ ký hoặc Timestamp!");
        }

        try {
            // 1. Lấy dữ liệu nguyên thủy
            byte[] rawBodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
            String secretKey = systemPaymentService.getWebhookSecret().trim();
            String actualSignature = signature.replace("sha256=", "").trim();
            String bodyStr = new String(rawBodyBytes, StandardCharsets.UTF_8);

            // 2. Gộp chuỗi theo đúng chuẩn "Timestamp . Body" mà sếp vừa dò ra
            String payloadToHash = timestamp + "." + bodyStr;
            byte[] dataToHash = payloadToHash.getBytes(StandardCharsets.UTF_8);

            // 3. Băm bằng Key String thông thường
            String myCalculatedSignature = calculateHmacSHA256(dataToHash, secretKey);

            if (!actualSignature.equals(myCalculatedSignature)) {
                return ResponseEntity.status(401).body("Chữ ký giả mạo!");
            }

            // 4. Pass bảo mật -> Xử lý nghiệp vụ
            SepayWebhookRequest webhookReq = objectMapper.readValue(rawBodyBytes, SepayWebhookRequest.class);
            systemPaymentService.processSepayWebhook(webhookReq);

            return ResponseEntity.ok("{\"success\": true}");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống Webhook");
        }
    }

    // Hàm băm giữ nguyên bản gốc String Key
    private String calculateHmacSHA256(byte[] dataBytes, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(dataBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa", e);
        }
    }

    // --- 4. CHECK TRẠNG THÁI GIAO DỊCH (Frontend / Kiosk gọi) ---
    @GetMapping("/{payCode}/status")
    public ResponseEntity<ApiResponse<String>> checkPaymentStatus(
            @PathVariable String payCode) {

        return ResponseEntity.ok(ApiResponse.success(
                "Kiểm tra trạng thái thành công",
                systemPaymentService.checkPaymentStatus(payCode)));
    }


}