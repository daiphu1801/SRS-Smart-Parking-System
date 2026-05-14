package com.smartparking.operation.controller.customer;

import com.smartparking.operation.dto.ParkingFeeQuote;
import com.smartparking.operation.service.ParkingFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/parking")
@RequiredArgsConstructor
public class CustomerParkingController {

    private final ParkingFeeService parkingFeeService;

    @GetMapping("/calculate-fee")
    public ResponseEntity<?> calculateFee(@RequestParam Long sessionId) {
        try {
            ParkingFeeQuote quote = parkingFeeService.calculateFee(sessionId);
            return ResponseEntity.ok(quote);
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null ? "invalid request" : ex.getMessage();
            if (message.contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
    }
}
