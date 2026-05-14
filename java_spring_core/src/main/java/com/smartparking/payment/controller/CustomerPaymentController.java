package com.smartparking.payment.controller;

import com.smartparking.payment.dto.CheckoutUrlRequest;
import com.smartparking.payment.dto.CheckoutUrlResponse;
import com.smartparking.payment.dto.PaymentHistoryItem;
import com.smartparking.payment.service.PaymentCheckoutService;
import com.smartparking.payment.service.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/payments")
@RequiredArgsConstructor
public class CustomerPaymentController {

    private final PaymentCheckoutService checkoutService;
    private final PaymentHistoryService historyService;

    @PostMapping("/checkout-url")
    public ResponseEntity<?> checkoutUrl(@RequestBody CheckoutUrlRequest body) {
        try {
            CheckoutUrlResponse res = checkoutService.createCheckoutUrl(body);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Integer accountId) {
        try {
            List<PaymentHistoryItem> items = historyService.listByAccountId(accountId);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "invalid request" : ex.getMessage();
            if (msg.contains("not found")) return ResponseEntity.notFound().build();
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }
}
