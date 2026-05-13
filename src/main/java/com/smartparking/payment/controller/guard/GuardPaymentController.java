package com.smartparking.payment.controller.guard;

import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.service.GuardPaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/guard/payments")
@RequiredArgsConstructor
public class GuardPaymentController {

    // Nếu ông gộp hàm processCashPayment vào PaymentService thì đổi tên ở đây nhé
    private final GuardPaymentService guardPaymentService;

    @PostMapping("/cash")
    public ResponseEntity<?> confirmCash(@Valid @RequestBody CashPaymentRequest request) {
        log.info("Guard is processing cash payment for session: {}", request.getSessionId());

        try {
            // Gọi hàm xử lý cốt lõi đã viết ở lượt trước
            Map<String, Object> result = guardPaymentService.processCashPayment(
                    request.getSessionId(),
                    request.getAmount());

            // Phân loại HTTP Status dựa trên cờ 'success' trả về từ Service
            boolean isSuccess = (boolean) result.getOrDefault("success", false);
            if (isSuccess) {
                return ResponseEntity.ok(result); // 200 OK: Trả tiền đủ, Barie mở
            } else {
                return ResponseEntity.badRequest().body(result); // 400 Bad Request: Lỗi nghiệp vụ (VD: Trả thiếu)
            }

        } catch (RuntimeException ex) {
            log.error("Lỗi khi thu tiền mặt: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }
    }

    // DTO Chuẩn chỉnh, có Validation chặn ngay từ cửa
    @Data
    public static class CashPaymentRequest {

        @NotNull(message = "Mã cuốc xe (sessionId) không được để trống")
        private Long sessionId;

        @NotNull(message = "Số tiền thu không được để trống")
        @Positive(message = "Số tiền thu phải lớn hơn 0")
        private BigDecimal amount;

    }
}