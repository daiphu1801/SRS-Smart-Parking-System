package com.smartparking.payment.repository;

import com.smartparking.payment.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long>,
        JpaSpecificationExecutor<PaymentDetail> {

    List<PaymentDetail> findByPaymentId(Long paymentId);

    @Query("SELECT pd FROM PaymentDetail pd JOIN FETCH pd.bookingDetail WHERE pd.paymentId = :paymentId")
    List<PaymentDetail> findByPaymentIdWithBookingDetail(@Param("paymentId") Long paymentId);
}
