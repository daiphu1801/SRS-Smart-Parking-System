//package com.smartparking.identity.controller.customer;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//import java.util.Map;
//
//// Mock imports since we haven't built all the cross-module services yet
//import com.smartparking.operation.entity.ParkingSession;
//import com.smartparking.operation.repository.ParkingSessionRepository;
//import com.smartparking.subscription.entity.VehicleType;
//import com.smartparking.subscription.entity.Package;
//import com.smartparking.subscription.repository.VehicleTypeRepository;
//import com.smartparking.subscription.repository.PackageRepository;
//import com.smartparking.payment.entity.Payment;
//import com.smartparking.payment.repository.PaymentRepository;
//import com.smartparking.operation.entity.Booking;
//import com.smartparking.operation.repository.BookingRepository;
//
//@RestController
//@RequestMapping("/api/v1/customer")
//@RequiredArgsConstructor
//public class CustomerController {
//
//    private final ParkingSessionRepository sessionRepo;
//    private final VehicleTypeRepository vehicleTypeRepo;
//    private final PackageRepository packageRepo;
//    private final PaymentRepository paymentRepo;
//    private final BookingRepository bookingRepo;
//
//    @GetMapping("/vehicles")
//    public ResponseEntity<?> getVehicles() {
//        // Logic to fetch customer's vehicles
//        return ResponseEntity.ok(List.of());
//    }
//
//    @GetMapping("/parking-sessions")
//    public ResponseEntity<List<ParkingSession>> getSessions(@RequestAttribute("accountId") Integer accountId) {
//        return ResponseEntity.ok(sessionRepo.findByCustomerId(accountId));
//    }
//
//    @GetMapping("/parking/calculate-fee")
//    public ResponseEntity<?> calculateFee(@RequestParam String vehicleNo) {
//        return ResponseEntity.ok(Map.of("vehicleNo", vehicleNo, "fee", 15000));
//    }
//
//    @PostMapping("/payments/checkout-url")
//    public ResponseEntity<?> getCheckoutUrl(@RequestBody Map<String, Object> payload) {
//        return ResponseEntity.ok(Map.of("url", "https://checkout.payos.vn/12345"));
//    }
//
//    @GetMapping("/group-management/members")
//    public ResponseEntity<?> getGroupMembers() {
//        return ResponseEntity.ok(List.of());
//    }
//
//    @GetMapping("/group-management/vehicles")
//    public ResponseEntity<?> getGroupVehicles() {
//        return ResponseEntity.ok(List.of());
//    }
//
//    @GetMapping("/bookings")
//    public ResponseEntity<List<Booking>> getBookings() {
//        return ResponseEntity.ok(bookingRepo.findAll());
//    }
//
//    @PostMapping("/bookings")
//    public ResponseEntity<?> renewBooking(@RequestBody Map<String, Object> payload) {
//        return ResponseEntity.ok(Map.of("message", "Booking renewal initiated"));
//    }
//
//    @GetMapping("/payments")
//    public ResponseEntity<List<Payment>> getPayments() {
//        return ResponseEntity.ok(paymentRepo.findAll());
//    }
//
//    @GetMapping("/vehicle-types")
//    public ResponseEntity<List<VehicleType>> getVehicleTypes() {
//        return ResponseEntity.ok(vehicleTypeRepo.findAll());
//    }
//
//    @GetMapping("/eligible-packages")
//    public ResponseEntity<List<Package>> getEligiblePackages() {
//        return ResponseEntity.ok(packageRepo.findAll());
//    }
//}
