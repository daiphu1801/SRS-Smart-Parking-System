package com.smartparking.controller;

import com.smartparking.config.SettingsService;
import com.smartparking.repository.PaymentRepository;
import com.smartparking.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SettingsService settingsService;
    private final PaymentRepository paymentRepo;
    private final ParkingSessionRepository sessionRepo;

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(Map.of(
            "grace_period_minutes", settingsService.getGracePeriodMinutes(),
            "safety_buffer_percent", settingsService.getSafetyBufferPercent(),
            "lpr_confidence_threshold", settingsService.getLprConfidenceThreshold(),
            "alert_threshold_seconds", settingsService.getAlertThresholdSeconds(),
            "payment_qr_expiry_seconds", settingsService.getPaymentQrExpirySeconds()
        ));
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> newSettings) {
        settingsService.saveAndReload(newSettings);
        return ResponseEntity.ok(Map.of("message", "Settings updated and hot-reloaded"));
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<?> revenueReport() {
        var all = paymentRepo.findAll();
        double total = all.stream().filter(p -> p.getStatus().name().equals("SUCCESS"))
            .mapToDouble(p -> p.getAmount().doubleValue()).sum();
        double cash = all.stream().filter(p -> p.getStatus().name().equals("SUCCESS")
            && p.getMethod().name().equals("CASH"))
            .mapToDouble(p -> p.getAmount().doubleValue()).sum();
        return ResponseEntity.ok(Map.of(
            "totalRevenue", total, "cashRevenue", cash,
            "onlineRevenue", total - cash, "transactionCount", all.size()
        ));
    }

    @GetMapping("/reports/audit")
    public ResponseEntity<?> auditLog() {
        return ResponseEntity.ok(
            sessionRepo.findAll().stream().filter(s -> Boolean.TRUE.equals(s.getFlagManual())).toList()
        );
    }
}
