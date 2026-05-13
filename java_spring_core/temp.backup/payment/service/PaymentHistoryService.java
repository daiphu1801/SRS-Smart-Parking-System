//package com.smartparking.payment.service;
//
//import com.smartparking.identity.entity.Customer;
//import com.smartparking.identity.repository.CustomerRepository;
//import com.smartparking.operation.entity.BookingDetail;
//import com.smartparking.operation.entity.ParkingSession;
//import com.smartparking.operation.repository.BookingDetailRepository;
//import com.smartparking.operation.repository.ParkingSessionRepository;
//import com.smartparking.payment.dto.PaymentHistoryItem;
//import com.smartparking.payment.entity.Payment;
//import com.smartparking.payment.entity.PaymentDetail;
//import com.smartparking.payment.repository.PaymentDetailRepository;
//import com.smartparking.payment.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentHistoryService {
//
//    private final CustomerRepository customerRepo;
//    private final BookingDetailRepository bookingDetailRepo;
//    private final ParkingSessionRepository sessionRepo;
//    private final PaymentDetailRepository paymentDetailRepo;
//    private final PaymentRepository paymentRepo;
//
//    public List<PaymentHistoryItem> listByAccountId(Integer accountId) {
//        if (accountId == null) throw new IllegalArgumentException("accountId is required");
//
//        Customer customer = customerRepo.findByAccountId(accountId)
//                .orElseThrow(() -> new IllegalArgumentException("customer not found"));
//
//        List<BookingDetail> bookingDetails = bookingDetailRepo.findByCustomerId(customer.getId());
//        if (bookingDetails.isEmpty()) return List.of();
//
//        List<Integer> bookingDetailIds = bookingDetails.stream().map(BookingDetail::getId).toList();
//        Map<Long, PaymentDetail> detailMap = new LinkedHashMap<>();
//        for (PaymentDetail detail : paymentDetailRepo.findByBookingDetailIdIn(bookingDetailIds)) {
//            detailMap.put(detail.getId(), detail);
//        }
//
//        Set<String> vehicleNos = bookingDetails.stream()
//                .map(BookingDetail::getVehicleNo)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        Set<Long> sessionIds = new HashSet<>();
//        for (ParkingSession session : sessionRepo.findByCustomerId(customer.getId())) {
//            sessionIds.add(session.getId());
//        }
//        if (!vehicleNos.isEmpty()) {
//            for (ParkingSession session : sessionRepo.findByVehicleNoInOrderByEntryTimeDesc(vehicleNos)) {
//                sessionIds.add(session.getId());
//            }
//        }
//
//        if (!sessionIds.isEmpty()) {
//            for (PaymentDetail detail : paymentDetailRepo.findByParkingSessionIdIn(sessionIds)) {
//                detailMap.put(detail.getId(), detail);
//            }
//        }
//
//        List<PaymentDetail> details = new ArrayList<>(detailMap.values());
//        if (details.isEmpty()) return List.of();
//
//        Set<Long> paymentIds = details.stream().map(PaymentDetail::getPaymentId).collect(Collectors.toSet());
//        Map<Long, Payment> payments = paymentRepo.findAllById(paymentIds).stream()
//                .collect(Collectors.toMap(Payment::getId, p -> p));
//
//        return details.stream().map(d -> {
//            Payment p = payments.get(d.getPaymentId());
//            return new PaymentHistoryItem(
//                    d.getPaymentId(),
//                    p != null ? p.getPayCode() : null,
//                    p != null ? p.getAmount() : null,
//                    p != null && p.getMethod() != null ? p.getMethod().name() : null,
//                    p != null && p.getStatus() != null ? p.getStatus().name() : null,
//                    p != null ? p.getCreatedAt() : null,
//                    d.getId(),
//                    d.getBookingDetailId(),
//                    d.getParkingSessionId(),
//                    d.getItemAmount(),
//                    d.getAppliedStartDate(),
//                    d.getAppliedEndDate()
//            );
//        }).toList();
//    }
//}
