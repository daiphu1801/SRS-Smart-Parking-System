package com.smartparking.payment.service;

import com.smartparking.payment.dto.request.payment.PaymentDetailFilterRequest;
import com.smartparking.payment.dto.response.payment.PaymentDetailResponse;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.specification.PaymentDetailSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class PaymentDetailService {

    private final PaymentDetailRepository paymentDetailRepository;

    public Page<PaymentDetailResponse> searchPaymentDetails(PaymentDetailFilterRequest filter, Pageable pageable) {

        Specification<PaymentDetail> spec = Specification
                .where(PaymentDetailSpecs.hasPaymentId(filter.getPaymentId()))
                .and(PaymentDetailSpecs.hasBookingDetailId(filter.getBookingDetailId()))
                .and(PaymentDetailSpecs.hasItemAmountGreaterThanEqual(filter.getMinItemAmount()))
                .and(PaymentDetailSpecs.hasItemAmountLessThanEqual(filter.getMaxItemAmount()))
                .and(PaymentDetailSpecs.hasAppliedStartDateFrom(filter.getAppliedStartDateFrom()))
                .and(PaymentDetailSpecs.hasAppliedStartDateTo(filter.getAppliedStartDateTo()))
                .and(PaymentDetailSpecs.hasAppliedEndDateFrom(filter.getAppliedEndDateFrom()))
                .and(PaymentDetailSpecs.hasAppliedEndDateTo(filter.getAppliedEndDateTo()));

        return paymentDetailRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    // 2. Lấy chi tiết 1 bản ghi
    public PaymentDetailResponse getPaymentDetailById(Long id) {
        PaymentDetail detail = paymentDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết thanh toán với ID: " + id));

        return mapToResponse(detail);
    }

    // --- HELPER FUNCTION ---
    private PaymentDetailResponse mapToResponse(PaymentDetail detail) {
        return PaymentDetailResponse.builder()
                .id(detail.getId())
                .paymentId(detail.getPaymentId())
                .bookingDetailId(detail.getBookingDetailId())
                .itemAmount(detail.getItemAmount())
                .appliedStartDate(detail.getAppliedStartDate())
                .appliedEndDate(detail.getAppliedEndDate())
                .build();
    }
//
//    public List<PaymentDetail> listByPaymentId(Long paymentId) {
//        return repo.findByPaymentId(paymentId);
//    }
//
//    public PaymentDetail createForSession(Long paymentId, Long sessionId, BigDecimal amount) {
//        PaymentDetail detail = PaymentDetail.builder()
//                .paymentId(paymentId)
//                .parkingSessionId(sessionId)
//                .itemAmount(amount)
//                .build();
//        return repo.save(detail);
//    }
//
//    public PaymentDetail createForBookingDetail(Long paymentId, Integer bookingDetailId,
//                                                BigDecimal amount, LocalDateTime start, LocalDateTime end) {
//        PaymentDetail detail = PaymentDetail.builder()
//                .paymentId(paymentId)
//                .bookingDetailId(bookingDetailId)
//                .itemAmount(amount)
//                .appliedStartDate(start)
//                .appliedEndDate(end)
//                .build();
//        return repo.save(detail);
//    }
}
