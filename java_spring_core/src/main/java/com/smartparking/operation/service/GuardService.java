//package com.smartparking.operation.service;
//
//import com.smartparking.operation.entity.ParkingSession;
//import com.smartparking.operation.repository.ParkingSessionRepository;
//import com.smartparking.payment.entity.Payment;
//import com.smartparking.payment.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import java.math.BigDecimal;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class GuardService {
//
//    private final ParkingSessionRepository sessionRepo;
//    private final PaymentRepository paymentRepo;
//
//    public BigDecimal calculateFee(String vehicleNo) {
//        // Mock fee calculation logic
//        log.info("Calculating fee for vehicle {}", vehicleNo);
//        return new BigDecimal("20000"); // 20k VND mock
//    }
//
//    public Payment confirmCashPayment(Long sessionId, BigDecimal amount, Integer empId) {
//        log.info("Confirming cash payment of {} for session {} by guard {}", amount, sessionId, empId);
//        Payment payment = new Payment();
//        payment.setAmount(amount);
//        payment.setStatus(com.smartparking.payment.entity.PaymentStatus.SUCCESS);
//        payment.setMethod(com.smartparking.payment.entity.PaymentMethod.CASH);
//        payment.setCreatedByEmpId(empId);
//        return paymentRepo.save(payment);
//    }
//
//    public void manualOpenBarrier(Integer deviceId, String reason) {
//        log.info("Guard manually opened barrier {} due to: {}", deviceId, reason);
//    }
//
//    public ParkingSession updateVehicleNo(Long sessionId, String newVehicleNo) {
//        ParkingSession session = sessionRepo.findById(sessionId)
//            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
//        session.setVehicleNo(newVehicleNo);
//        return sessionRepo.save(session);
//    }
//}
