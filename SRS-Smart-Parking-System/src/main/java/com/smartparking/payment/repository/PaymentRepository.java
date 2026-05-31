package com.smartparking.payment.repository;

import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment>,
        PaymentDashboardCustomRepository {
    List<Payment> findByStatus(Status status);

    @Query("SELECT p FROM Payment p WHERE :content LIKE CONCAT('%', p.payCode, '%')")
    List<Payment> findPaymentByTransferContent(@Param("content") String content);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = :newStatus, p.updatedAt = CURRENT_TIMESTAMP WHERE p.parkingSessionId = :sessionId AND p.status = :oldStatus")
    int cancelPendingPayments(Long sessionId, Status oldStatus, Status newStatus);

    boolean existsByParkingSessionIdAndAmountAndStatus(Long parkingSessionId, BigDecimal amount, Status status);

    @Query("SELECT p FROM Payment p WHERE " +
            // KỊCH BẢN 1: Bất kỳ đơn nào đang nằm ở MANUAL_SUCCESS đều lôi ra hết
            "p.status = com.smartparking.payment.entity.Status.MANUAL_SUCCESS " +
            "OR " +
            // KỊCH BẢN 2: Lệch tiền hoặc cần chú ý
            "p.status IN (com.smartparking.payment.entity.Status.PARTIAL_PAYMENT, com.smartparking.payment.entity.Status.NEEDS_ATTENTION) " +
            "ORDER BY p.updatedAt DESC")
    Page<Payment> findReconciliationExceptions(Pageable pageable);

    boolean existsByParkingSessionIdAndAmountAndStatusNotIn(
            Long parkingSessionId,
            BigDecimal amount,
            List<Status> excludedStatuses
    );
}
