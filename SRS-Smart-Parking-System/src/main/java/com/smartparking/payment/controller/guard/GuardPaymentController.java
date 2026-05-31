package com.smartparking.payment.controller.guard;

import com.smartparking.payment.dto.request.CashPaymentRequest;
import com.smartparking.payment.dto.request.ManualBankTransferRequest;
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

    private final GuardPaymentService guardPaymentService;

    @PostMapping("/cash")
    public ResponseEntity<?> confirmCash(@Valid @RequestBody CashPaymentRequest request) {

        try {
            Map<String, Object> result = guardPaymentService.processCashPayment(
                    request.getVehicleNo(),
                    request.getAmount());

            boolean isSuccess = (boolean) result.getOrDefault("success", false);
            if (isSuccess) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);}

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping("/bank-transfer")
    public ResponseEntity<?> confirmManualBankTransfer(@Valid @RequestBody ManualBankTransferRequest request) {

        try {
            Map<String, Object> result = guardPaymentService.processManualBankTransfer(
                    request.getVehicleNo(),
                    request.getAmount(),
                    request.getNote()
            );
            boolean isSuccess = (boolean) result.getOrDefault("success", false);
            if (isSuccess) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (RuntimeException ex) {
            log.error("[❌ MANUAL BANK ERROR] Lỗi xác nhận CK thủ công cho xe {}: {}", request.getVehicleNo(), ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }
    }


}
