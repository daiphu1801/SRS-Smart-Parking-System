package com.smartparking.payment.specification;

import com.smartparking.payment.entity.PaymentDetail;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDetailSpecs {
    public static Specification<PaymentDetail> hasPaymentId(Long paymentId) {
        return (root, query, cb) -> paymentId != null
                ? cb.equal(root.get("paymentId"), paymentId)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasBookingDetailId(Integer bookingDetailId) {
        return (root, query, cb) -> bookingDetailId != null
                ? cb.equal(root.get("bookingDetailId"), bookingDetailId)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasParkingSessionId(Long parkingSessionId) {
        return (root, query, cb) -> parkingSessionId != null
                ? cb.equal(root.get("parkingSessionId"), parkingSessionId)
                : cb.conjunction();
    }

    // --- Amount Range Specifications ---
    public static Specification<PaymentDetail> hasItemAmountGreaterThanEqual(BigDecimal minAmount) {
        return (root, query, cb) -> minAmount != null
                ? cb.greaterThanOrEqualTo(root.get("itemAmount"), minAmount)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasItemAmountLessThanEqual(BigDecimal maxAmount) {
        return (root, query, cb) -> maxAmount != null
                ? cb.lessThanOrEqualTo(root.get("itemAmount"), maxAmount)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasAppliedStartDateFrom(LocalDateTime fromDate) {
        return (root, query, cb) -> fromDate != null
                ? cb.greaterThanOrEqualTo(root.get("appliedStartDate"), fromDate)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasAppliedStartDateTo(LocalDateTime toDate) {
        return (root, query, cb) -> toDate != null
                ? cb.lessThanOrEqualTo(root.get("appliedStartDate"), toDate)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasAppliedEndDateFrom(LocalDateTime fromDate) {
        return (root, query, cb) -> fromDate != null
                ? cb.greaterThanOrEqualTo(root.get("appliedEndDate"), fromDate)
                : cb.conjunction();
    }

    public static Specification<PaymentDetail> hasAppliedEndDateTo(LocalDateTime toDate) {
        return (root, query, cb) -> toDate != null
                ? cb.lessThanOrEqualTo(root.get("appliedEndDate"), toDate)
                : cb.conjunction();
    }
}