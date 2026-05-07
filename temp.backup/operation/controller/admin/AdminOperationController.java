package com.smartparking.operation.controller.admin;

import com.smartparking.operation.entity.*;
import com.smartparking.operation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminOperationController {

    private final ZoneRepository zoneRepo;
    private final IoTDeviceRepository deviceRepo;
    private final BookingRepository bookingRepo;
    private final BookingDetailRepository bookingDetailRepo;

    // --- 2.4 Bản đồ hầm & Thiết bị ---
    @GetMapping("/zones")
    public ResponseEntity<List<Zone>> listZones() {
        return ResponseEntity.ok(zoneRepo.findAll());
    }

    @GetMapping("/devices")
    public ResponseEntity<List<IoTDevice>> listDevices() {
        return ResponseEntity.ok(deviceRepo.findAll());
    }

    @PostMapping("/devices/{id}/barrier-control")
    public ResponseEntity<?> controlBarrier(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        // Mock sending command to IoT device
        return ResponseEntity.ok(Map.of("message", "Command " + body.get("command") + " sent to barrier " + id));
    }

    // --- 2.5 Hợp đồng đỗ xe ---
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> listBookings() {
        return ResponseEntity.ok(bookingRepo.findAll());
    }

    @GetMapping("/booking-details")
    public ResponseEntity<List<BookingDetail>> listBookingDetails() {
        return ResponseEntity.ok(bookingDetailRepo.findAll());
    }
}
