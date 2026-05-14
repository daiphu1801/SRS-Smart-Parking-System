package com.smartparking.payment.controller.system;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.payment.dto.request.payment.PaymentBookingRequest;
import com.smartparking.payment.dto.request.payment.PaymentSessionRequest;
import com.smartparking.payment.dto.request.payment.SepayWebhookRequest;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.payment.service.SystemPaymentService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/payments")
@RequiredArgsConstructor
public class SystemPaymentController {

    private final SystemPaymentService systemPaymentService;

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
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody SepayWebhookRequest request) {

        // 1. Kiểm tra bảo mật (SePay gửi kèm token trong Header)
        // Format Header: "Apikey change_me_in_production"
        String expectedHeader = "Apikey " + systemPaymentService.getWebhookSecret();
        if (!expectedHeader.equals(authorizationHeader)) {
            return ResponseEntity.status(401).body("Sai Token bảo mật!");
        }

        // 2. Gọi Service xử lý
        systemPaymentService.processSepayWebhook(request);

        // 3. Trả về cho SePay biết là "Tôi đã nhận được tiền"
        return ResponseEntity.ok("{\"success\": true}");
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