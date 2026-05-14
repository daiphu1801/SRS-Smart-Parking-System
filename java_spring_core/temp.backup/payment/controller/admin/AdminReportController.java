//package com.smartparking.payment.controller.admin;
//
//import com.smartparking.operation.entity.ParkingSession;
//import com.smartparking.operation.repository.ParkingSessionRepository;
//import com.smartparking.payment.entity.Payment;
//import com.smartparking.payment.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/admin")
//@RequiredArgsConstructor
//public class AdminReportController {
//
//    private final ParkingSessionRepository sessionRepo;
//    private final PaymentRepository paymentRepo;
//
//    // --- 2.6 Báo cáo & Tra cứu ---
//    @GetMapping("/parking-sessions")
//    public ResponseEntity<List<ParkingSession>> listSessions() {
//        return ResponseEntity.ok(sessionRepo.findAll());
//    }
//
//    @GetMapping("/parking-sessions/{id}")
//    public ResponseEntity<?> getSessionDetail(@PathVariable Long id) {
//        return sessionRepo.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/payments")
//    public ResponseEntity<List<Payment>> listPayments() {
//        return ResponseEntity.ok(paymentRepo.findAll());
//    }
//
//    // @GetMapping("/payments/{id}")
//    // public ResponseEntity<?> getPaymentDetail(@PathVariable Integer id) {
//    // return paymentRepo.findById(id)
//    // .map(ResponseEntity::ok)
//    // .orElse(ResponseEntity.notFound().build());
//    // }
//
//    @GetMapping("/reports/revenue")
//    public ResponseEntity<?> revenueReport() {
//        return ResponseEntity.ok(Map.of("message", "Revenue report under construction"));
//    }
//}
