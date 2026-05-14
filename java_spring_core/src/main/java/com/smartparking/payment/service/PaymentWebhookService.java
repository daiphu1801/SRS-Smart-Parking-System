package com.smartparking.payment.service;

import com.smartparking.shared.config.SettingsService;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.entity.PaymentGateway;
import com.smartparking.payment.entity.PaymentStatus;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PaymentRepository paymentRepo;
    private final PaymentDetailRepository detailRepo;
    private final ParkingSessionRepository sessionRepo;
    private final BookingDetailRepository bookingDetailRepo;
    private final SettingsService settings;
    private final com.smartparking.shared.integration.SePayService sePayService;

    @Transactional
    public String handleWebhook(String gateway, Map<String, String> headers, Map<String, Object> payload) {
        if ("SEPAY".equalsIgnoreCase(gateway)) {
            String authHeader = headers.get("authorization");
            if (!sePayService.isValidWebhook(authHeader)) {
                throw new IllegalArgumentException("Invalid SePay webhook signature");
            }
        }

        String payCode = extractPayCode(gateway, payload);
        if (payCode == null) return "ignored";

        Payment payment = paymentRepo.findByPayCode(payCode).orElse(null);
        if (payment == null) return "ignored";

        PaymentStatus newStatus = resolveStatus(payload);
        if (payment.getStatus() == PaymentStatus.SUCCESS && newStatus == PaymentStatus.SUCCESS) {
            return "ok";
        }

        if (gateway != null && payment.getGateway() == null) {
            try { payment.setGateway(PaymentGateway.valueOf(gateway.toUpperCase())); }
            catch (Exception ignored) { }
        }

        payment.setStatus(newStatus);
        payment.setGatewayResponse(payload);
        paymentRepo.save(payment);

        if (newStatus == PaymentStatus.SUCCESS) {
            applySuccess(payment.getId());
        }
        return "ok";
    }

    private void applySuccess(Long paymentId) {
        List<PaymentDetail> details = detailRepo.findByPaymentId(paymentId);
        for (PaymentDetail d : details) {
            if (d.getParkingSessionId() != null) {
                sessionRepo.findById(d.getParkingSessionId()).ifPresent(session -> {
                    session.setIsPaid(true);
                    session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(settings.getGracePeriodMinutes()));
                    sessionRepo.save(session);
                });
            }
            if (d.getBookingDetailId() != null) {
                bookingDetailRepo.findById(d.getBookingDetailId()).ifPresent(bd -> {
                    bd.setStatus(BookingStatus.ACTIVE);
                    if (d.getAppliedStartDate() != null) bd.setStartDate(d.getAppliedStartDate());
                    if (d.getAppliedEndDate() != null) bd.setEndDate(d.getAppliedEndDate());
                    bookingDetailRepo.save(bd);
                });
            }
        }
    }

    private String extractPayCode(String gateway, Map<String, Object> payload) {
        if ("SEPAY".equalsIgnoreCase(gateway)) {
            return sePayService.extractPaymentCode(payload);
        }
        Object code = payload.get("payCode");
        if (code == null) code = payload.get("pay_code");
        if (code == null) code = payload.get("orderCode");
        if (code == null) code = payload.get("order_code");
        if (code == null) code = payload.get("transactionCode");
        return code != null ? code.toString() : null;
    }

    private PaymentStatus resolveStatus(Map<String, Object> payload) {
        Object status = payload.get("status");
        if (status == null) status = payload.get("payment_status");
        if (status == null) return PaymentStatus.SUCCESS;
        String value = status.toString().toUpperCase();
        try { return PaymentStatus.valueOf(value); }
        catch (Exception ex) { return PaymentStatus.SUCCESS; }
    }
}
