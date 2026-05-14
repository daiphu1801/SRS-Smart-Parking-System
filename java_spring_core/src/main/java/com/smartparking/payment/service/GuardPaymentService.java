package com.smartparking.payment.service;

import com.smartparking.shared.config.SettingsService;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.PaymentStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.repository.PackagePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuardPaymentService {

    private final PaymentRepository paymentRepo;
    private final ParkingSessionRepository sessionRepo;
    private final BookingDetailRepository bookingDetailRepo;
    private final PackagePriceRepository packagePriceRepo;
    private final SettingsService settings;
    private final PaymentDetailService paymentDetailService;

    @Transactional
    public Payment confirmCashPayment(String targetType, Long targetId, Integer guardEmpId, BigDecimal amount) {
        if (targetType == null || targetId == null) throw new IllegalArgumentException("targetType and targetId are required");
        if (guardEmpId == null) throw new IllegalArgumentException("guardEmpId is required");

        Payment payment = Payment.builder()
                .payCode("CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESS)
                .createdByEmpId(guardEmpId)
                .build();

        if ("SESSION".equalsIgnoreCase(targetType)) {
            ParkingSession session = sessionRepo.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("session not found"));
            
            if (Boolean.TRUE.equals(session.getIsPaid())) {
                throw new IllegalArgumentException("session already paid");
            }
            
            BigDecimal payAmount = amount != null && amount.compareTo(BigDecimal.ZERO) > 0 ? amount : session.getAmountDue();
            if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amount must be > 0");
            }
            
            payment.setAmount(payAmount);
            paymentRepo.save(payment);
            paymentDetailService.createForSession(payment.getId(), session.getId(), payAmount);

            session.setIsPaid(true);
            session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(settings.getGracePeriodMinutes()));
            session.setFlagManual(true);
            sessionRepo.save(session);
            
        } else if ("BOOKING_DETAIL".equalsIgnoreCase(targetType)) {
            Integer bookingDetailId = targetId.intValue();
            BookingDetail bd = bookingDetailRepo.findById(bookingDetailId)
                    .orElseThrow(() -> new IllegalArgumentException("booking detail not found"));
            PackagePrice pp = packagePriceRepo.findById(bd.getPackagePriceId())
                    .orElseThrow(() -> new IllegalArgumentException("package price not found"));
            
            BigDecimal payAmount = amount != null && amount.compareTo(BigDecimal.ZERO) > 0 ? amount : pp.getPrice();
            if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amount must be > 0");
            }
            
            payment.setAmount(payAmount);
            paymentRepo.save(payment);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newStart = bd.getEndDate() != null && bd.getEndDate().isAfter(now) ? bd.getEndDate() : now;
            LocalDateTime newEnd = newStart.plusMonths(pp.getDurationMonths());
            
            paymentDetailService.createForBookingDetail(payment.getId(), bookingDetailId, payAmount, newStart, newEnd);
            
            bd.setStatus(BookingStatus.ACTIVE);
            bd.setStartDate(newStart);
            bd.setEndDate(newEnd);
            bookingDetailRepo.save(bd);
            
        } else {
            throw new IllegalArgumentException("targetType must be SESSION or BOOKING_DETAIL");
        }

        return payment;
    }
}
