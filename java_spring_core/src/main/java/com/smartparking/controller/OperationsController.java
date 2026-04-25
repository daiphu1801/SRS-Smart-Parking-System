package com.smartparking.controller;

import com.smartparking.entity.BookingDetail;
import com.smartparking.repository.BookingDetailRepository;
import com.smartparking.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OperationsController {

    private final ParkingSessionRepository sessionRepo;
    private final BookingDetailRepository bookingDetailRepo;

    @GetMapping("/api/customer/sessions")
    public ResponseEntity<?> getCustomerSessions(@AuthenticationPrincipal Integer accountId) {
        return ResponseEntity.ok(sessionRepo.findByCustomerId(accountId));
    }

    @GetMapping("/api/kiosk/sessions/lookup")
    public ResponseEntity<?> kioskLookup(@RequestParam String plate) {
        return sessionRepo.findOpenSession(plate)
            .map(s -> ResponseEntity.ok((Object) s))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/guard/sessions/lookup")
    public ResponseEntity<?> guardLookup(@RequestParam String plate) {
        var sessions = sessionRepo.findByVehicleNoOrderByEntryTimeDesc(plate);
        var sub = bookingDetailRepo.findActiveByVehicleNo(plate, LocalDateTime.now());
        return ResponseEntity.ok(Map.of(
            "sessions", sessions,
            "activeSubscription", sub.isPresent(),
            "subscriptionEndDate", sub.map(BookingDetail::getEndDate).orElse(null)
        ));
    }
}
