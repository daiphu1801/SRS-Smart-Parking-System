package com.smartparking.payment.controller.guard;

import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.service.GuardPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/guard/payments")
@RequiredArgsConstructor
public class GuardPaymentController {

    private final GuardPaymentService guardPaymentService;

    @PostMapping("/cash")
    public ResponseEntity<?> confirmCash(@RequestBody CashPaymentRequest body) {
        try {
            Payment payment = guardPaymentService.confirmCashPayment(
                    body.targetType, body.targetId, body.guardEmpId, body.amount);
            return ResponseEntity.ok(Map.of(
                    "paymentId", payment.getId(),
                    "payCode", payment.getPayCode(),
                    "amount", payment.getAmount(),
                    "status", payment.getStatus()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    static class CashPaymentRequest {
        public String targetType;
        public Long targetId;
        public Integer guardEmpId;
        public BigDecimal amount;
    }
}
