package com.smartparking.payment.service;

import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.repository.PaymentDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentDetailService {

    private final PaymentDetailRepository repo;

    public List<PaymentDetail> listByPaymentId(Long paymentId) {
        return repo.findByPaymentId(paymentId);
    }

    public PaymentDetail createForSession(Long paymentId, Long sessionId, BigDecimal amount) {
        PaymentDetail detail = PaymentDetail.builder()
                .paymentId(paymentId)
                .parkingSessionId(sessionId)
                .itemAmount(amount)
                .build();
        return repo.save(detail);
    }

    public PaymentDetail createForBookingDetail(Long paymentId, Integer bookingDetailId,
                                                BigDecimal amount, LocalDateTime start, LocalDateTime end) {
        PaymentDetail detail = PaymentDetail.builder()
                .paymentId(paymentId)
                .bookingDetailId(bookingDetailId)
                .itemAmount(amount)
                .appliedStartDate(start)
                .appliedEndDate(end)
                .build();
        return repo.save(detail);
    }
}
