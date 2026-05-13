package com.smartparking.payment.repository;

import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment> {
    List<Payment> findByStatus(Status status);

    @Query("SELECT p FROM Payment p WHERE :content LIKE CONCAT('%', p.payCode, '%')")
    List<Payment> findPaymentByTransferContent(@Param("content") String content);
}
