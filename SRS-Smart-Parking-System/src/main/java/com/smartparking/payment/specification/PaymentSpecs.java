package com.smartparking.payment.specification;

import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.Status;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentSpecs {
    // --- 1. Specifications with Customer Joins ---
    public static Specification<Payment> hasCustomerPhone(String phone) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(phone)) return cb.conjunction();

            // Perform a LEFT JOIN to ensure transient customers (without IDs) are still included in the results
            Join<Object, Object> customerJoin = root.join("customer", JoinType.LEFT);
            return cb.like(customerJoin.get("phone"), "%" + phone.trim() + "%");
        };
    }

    public static Specification<Payment> hasCustomerId(Integer customerId) {
        return (root, query, cb) -> customerId != null
                ? cb.equal(root.get("payerId"), customerId)
                : cb.conjunction();
    }
    public static Specification<Payment> hasSessionId(Long sessionId) {
        return (root, query, cb) -> sessionId != null
                ? cb.equal(root.get("sessionId"), sessionId)
                : cb.conjunction();
    }

    // --- 2. Basic Field Specifications ---
    public static Specification<Payment> hasPayCode(String payCode) {
        return (root, query, cb) -> StringUtils.hasText(payCode)
                ? cb.like(root.get("payCode"), "%" + payCode.trim() + "%")
                : cb.conjunction();
    }

    public static Specification<Payment> hasGateway(String gateway) {
        return (root, query, cb) -> gateway != null
                ? cb.equal(root.get("gateway"), gateway)
                : cb.conjunction();
    }
    public static Specification<Payment> hasStatus(Status status) {
        return (root, query, cb) -> status != null
                ? cb.equal(root.get("status"), status)
                : cb.conjunction();
    }

    public static Specification<Payment> hasMethod(PaymentMethod method) {
        return (root, query, cb) -> method != null
                ? cb.equal(root.get("method"), method)
                : cb.conjunction();
    }

    // --- 3. Amount Range Specifications ---
    public static Specification<Payment> hasAmountGreaterThanEqual(BigDecimal minAmount) {
        return (root, query, cb) -> minAmount != null
                ? cb.greaterThanOrEqualTo(root.get("amount"), minAmount)
                : cb.conjunction();
    }

    public static Specification<Payment> hasAmountLessThanEqual(BigDecimal maxAmount) {
        return (root, query, cb) -> maxAmount != null
                ? cb.lessThanOrEqualTo(root.get("amount"), maxAmount)
                : cb.conjunction();
    }

    // --- 4. Date Range Specifications ---
    public static Specification<Payment> hasCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            if (to != null) return cb.lessThanOrEqualTo(root.get("createdAt"), to);
            return cb.conjunction();
        };
    }

    public static Specification<Payment> hasUpdatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) return cb.between(root.get("updatedAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("updatedAt"), from);
            if (to != null) return cb.lessThanOrEqualTo(root.get("updatedAt"), to);
            return cb.conjunction();
        };
    }

    public static Specification<Payment> belongsToGroupIds(List<Integer> memberGroupIds) {
        return (root, query, cb) -> {
            // IDOR Protection: Deny access if the user belongs to no groups
            if (memberGroupIds == null || memberGroupIds.isEmpty()) {
                return cb.disjunction(); // Return a FALSE predicate (1=0)
            }

            // MANDATORY: Use distinct to prevent Cartesian product duplicates from OneToMany joins
            query.distinct(true);

            // Chain joins: Payment -> PaymentDetail -> BookingDetail -> Booking
            Join<Object, Object> detailsJoin = root.join("details", JoinType.INNER);
            Join<Object, Object> bookingDetailJoin = detailsJoin.join("bookingDetail", JoinType.INNER);
            Join<Object, Object> bookingJoin = bookingDetailJoin.join("booking", JoinType.INNER);

            // Authorize by checking if the booking's group ID is within the customer's authorized list
            return bookingJoin.get("groupId").in(memberGroupIds);
        };
    }

    // Filter out junk or terminal transactions (e.g., EXPIRED, FAILED)
    public static Specification<Payment> isNotTrash() {
        return (root, query, cb) -> cb.not(root.get("status").in(Status.EXPIRED, Status.FAILED));
    }
}

