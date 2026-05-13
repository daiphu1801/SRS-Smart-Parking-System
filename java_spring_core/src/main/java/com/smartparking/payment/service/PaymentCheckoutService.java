package com.smartparking.payment.service;

import com.smartparking.payment.dto.CheckoutUrlRequest;
import com.smartparking.payment.dto.CheckoutUrlResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentGateway;
import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.PaymentStatus;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.DayType;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.repository.PackagePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCheckoutService {

    private final PaymentRepository paymentRepo;
    private final PaymentDetailService paymentDetailService;
    private final ParkingSessionRepository sessionRepo;
    private final BookingDetailRepository bookingDetailRepo;
    private final PackagePriceRepository packagePriceRepo;
    private final BillingCalculatorService billingCalculatorService;
    private final com.smartparking.shared.integration.SePayService sePayService;

    @Transactional
    public CheckoutUrlResponse createCheckoutUrl(CheckoutUrlRequest req) {
        if (req.getTargetType() == null || req.getTargetId() == null) {
            throw new IllegalArgumentException("targetType and targetId are required");
        }
        if (req.getMethod() == null) {
            throw new IllegalArgumentException("method is required");
        }

        PaymentMethod method = PaymentMethod.valueOf(req.getMethod().toUpperCase());
        if (method != PaymentMethod.PAYOS && method != PaymentMethod.VNPAY && method != PaymentMethod.SEPAY) {
            throw new IllegalArgumentException("method must be PAYOS, VNPAY, or SEPAY");
        }

        String targetType = req.getTargetType().toUpperCase();
        BigDecimal amount = resolveAmount(targetType, req.getTargetId(), req.getAmount());

        Payment payment = Payment.builder()
                .payCode("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .amount(amount)
                .method(method)
                .gateway(PaymentGateway.valueOf(method.name()))
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepo.save(payment);

        if ("SESSION".equals(targetType)) {
            paymentDetailService.createForSession(payment.getId(), req.getTargetId(), amount);
        } else if ("BOOKING_DETAIL".equals(targetType)) {
            Integer bookingDetailId = req.getTargetId().intValue();
            BookingDetail bd = bookingDetailRepo.findById(bookingDetailId)
                    .orElseThrow(() -> new IllegalArgumentException("booking detail not found"));
            PackagePrice pp = packagePriceRepo.findById(bd.getPackagePriceId())
                    .orElseThrow(() -> new IllegalArgumentException("package price not found"));
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newStart = bd.getEndDate() != null && bd.getEndDate().isAfter(now) ? bd.getEndDate() : now;
            LocalDateTime newEnd = newStart.plusMonths(pp.getDurationMonths());
            
            paymentDetailService.createForBookingDetail(
                    payment.getId(), bookingDetailId, amount, newStart, newEnd);
        } else {
            throw new IllegalArgumentException("targetType must be SESSION or BOOKING_DETAIL");
        }

        String checkoutUrl;
        if (method == PaymentMethod.SEPAY) {
            checkoutUrl = sePayService.createQrImageUrl(amount.longValue(), payment.getPayCode());
        } else {
            checkoutUrl = buildMockCheckoutUrl(method, payment.getPayCode(), amount);
        }
        return new CheckoutUrlResponse(payment.getPayCode(), checkoutUrl, amount, method.name(), payment.getStatus().name());
    }

    private BigDecimal resolveAmount(String targetType, Long targetId, BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) return amount;

        if ("SESSION".equals(targetType)) {
            ParkingSession session = sessionRepo.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("session not found"));
            if (session.getAmountDue() != null && session.getAmountDue().compareTo(BigDecimal.ZERO) > 0) {
                return session.getAmountDue();
            }
            LocalDateTime entry = session.getEntryTime();
            LocalDateTime exit = session.getExitTime() != null ? session.getExitTime() : LocalDateTime.now();
            DayType dayType = resolveDayType(exit);
            return billingCalculatorService.calculateGuestFee(entry, exit, session.getVehicleTypeId(), dayType);
        }

        if ("BOOKING_DETAIL".equals(targetType)) {
            Integer bookingDetailId = targetId.intValue();
            BookingDetail bd = bookingDetailRepo.findById(bookingDetailId)
                    .orElseThrow(() -> new IllegalArgumentException("booking detail not found"));
            PackagePrice pp = packagePriceRepo.findById(bd.getPackagePriceId())
                    .orElseThrow(() -> new IllegalArgumentException("package price not found"));
            return pp.getPrice();
        }

        throw new IllegalArgumentException("invalid targetType");
    }

    private DayType resolveDayType(LocalDateTime time) {
        DayOfWeek dow = time.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return DayType.WEEKEND;
        }
        return DayType.WEEKDAY;
    }

    private String buildMockCheckoutUrl(PaymentMethod method, String payCode, BigDecimal amount) {
        return String.format("https://checkout.local/%s?code=%s&amount=%s",
                method.name().toLowerCase(), payCode, amount.toPlainString());
    }
}
