package com.smartparking.controller;

import com.smartparking.config.SettingsService;
import com.smartparking.entity.Payment;
import com.smartparking.entity.PaymentMethod;
import com.smartparking.entity.PaymentStatus;
import com.smartparking.integration.SePayService;
import com.smartparking.repository.PaymentRepository;
import com.smartparking.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final ParkingSessionRepository sessionRepo;
    private final SePayService sePayService;
    private final SettingsService settings;

    @PostMapping("/qr/create")
    public ResponseEntity<?> createQrPayment(@RequestBody Map<String, Object> body) {
        String plate = (String) body.get("plate");
        var session = sessionRepo.findOpenSession(plate)
            .orElseThrow(() -> new IllegalArgumentException("No open session for: " + plate));

        String payCode = "SMARTPARK " + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = Payment.builder()
            .payCode(payCode).amount(session.getAmountDue())
            .method(PaymentMethod.SEPAY).status(PaymentStatus.PENDING).build();
        paymentRepo.save(payment);

        return ResponseEntity.ok(Map.of(
            "qrUrl", sePayService.createQrImageUrl(session.getAmountDue().longValue(), payCode),
            "amount", session.getAmountDue(),
            "payCode", payCode,
            "expiresInSeconds", settings.getPaymentQrExpirySeconds()
        ));
    }

    @PostMapping("/webhook/sepay")
    public ResponseEntity<?> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> payload) {

        if (!sePayService.isValidWebhook(auth)) {
            log.warn("Invalid SePay webhook signature");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String payCode = sePayService.extractPaymentCode(payload);
        if (payCode == null) return ResponseEntity.ok("ignored");

        paymentRepo.findByPayCode(payCode).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayResponse(payload);
            paymentRepo.save(payment);

            sessionRepo.findAll().stream()
                .filter(s -> s.getExitTime() == null && BigDecimal.ZERO.compareTo(s.getAmountDue()) != 0)
                .findFirst().ifPresent(session -> {
                    session.setIsPaid(true);
                    session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(settings.getGracePeriodMinutes()));
                    sessionRepo.save(session);
                });
        });

        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping("/cash")
    public ResponseEntity<?> confirmCashPayment(@RequestBody Map<String, Object> body) {
        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        Integer guardEmpId = (Integer) body.get("guardEmpId");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        Payment payment = Payment.builder()
            .payCode("CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .amount(amount).method(PaymentMethod.CASH).status(PaymentStatus.SUCCESS)
            .createdByEmpId(guardEmpId).build();
        paymentRepo.save(payment);

        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setIsPaid(true);
            s.setGracePeriodEnd(LocalDateTime.now().plusMinutes(settings.getGracePeriodMinutes()));
            s.setFlagManual(true);
            sessionRepo.save(s);
        });

        return ResponseEntity.ok(Map.of("payCode", payment.getPayCode()));
    }
}
