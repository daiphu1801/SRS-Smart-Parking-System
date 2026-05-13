package com.smartparking.worker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    @PostMapping("/webhooks/payment-legacy")
    public ResponseEntity<?> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received payment webhook: {}", payload);
        // Delegate to payment service
        return ResponseEntity.status(410).body(Map.of(
                "error", "Deprecated. Use POST /api/v1/system/webhooks/payment"
        ));
    }

    @PostMapping("/jobs/check-expired")
    public ResponseEntity<?> triggerCheckExpiredJob() {
        log.info("Triggering check-expired job");
        // Delegate to worker service
        return ResponseEntity.ok(Map.of("message", "Job check-expired triggered"));
    }

    @PostMapping("/jobs/clear-grace-periods")
    public ResponseEntity<?> triggerClearGracePeriodsJob() {
        log.info("Triggering clear-grace-periods job");
        // Delegate to worker service
        return ResponseEntity.ok(Map.of("message", "Job clear-grace-periods triggered"));
    }
}
