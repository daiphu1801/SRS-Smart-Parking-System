package com.smartparking.payment.controller;

import com.smartparking.payment.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/system/webhooks")
@RequiredArgsConstructor
public class SystemPaymentWebhookController {

    private final PaymentWebhookService webhookService;

    @PostMapping("/payment")
    public ResponseEntity<?> handlePaymentWebhook(
            @RequestParam(required = false) String gateway,
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            String result = webhookService.handleWebhook(gateway, headers, payload);
            return ResponseEntity.ok(Map.of("status", result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
        }
    }
}
