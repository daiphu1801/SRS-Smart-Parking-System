package com.smartparking.operation.controller.guard;

import com.smartparking.operation.service.GuardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/guard")
@RequiredArgsConstructor
public class GuardController {

    private final GuardService guardService;

    @GetMapping("/parking/calculate-fee")
    public ResponseEntity<?> calculateFee(@RequestParam String vehicleNo) {
        BigDecimal fee = guardService.calculateFee(vehicleNo);
        return ResponseEntity.ok(Map.of("vehicleNo", vehicleNo, "fee", fee));
    }

    @PostMapping("/payments/cash-legacy")
    public ResponseEntity<?> cashPayment(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(410).body(Map.of(
                "error", "Deprecated. Use POST /api/v1/guard/payments/cash"
        ));
    }

    @PostMapping("/parking/manual-open")
    public ResponseEntity<?> manualOpen(@RequestBody Map<String, Object> body) {
        Integer deviceId = (Integer) body.get("deviceId");
        String reason = (String) body.get("reason");
        guardService.manualOpenBarrier(deviceId, reason);
        return ResponseEntity.ok(Map.of("message", "Barrier opened manually"));
    }

    @PutMapping("/parking-sessions/{id}/vehicle-no")
    public ResponseEntity<?> updateVehicleNo(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(guardService.updateVehicleNo(id, body.get("vehicleNo")));
    }
}
