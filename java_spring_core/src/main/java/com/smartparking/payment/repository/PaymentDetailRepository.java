package com.smartparking.payment.repository;

import com.smartparking.payment.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;

@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
    List<PaymentDetail> findByPaymentId(Long paymentId);
    List<PaymentDetail> findByBookingDetailId(Integer bookingDetailId);
    List<PaymentDetail> findByParkingSessionId(Long parkingSessionId);

    List<PaymentDetail> findByBookingDetailIdIn(Collection<Integer> bookingDetailIds);
    List<PaymentDetail> findByParkingSessionIdIn(Collection<Long> parkingSessionIds);
    List<PaymentDetail> findByPaymentIdIn(Collection<Long> paymentIds);
}
