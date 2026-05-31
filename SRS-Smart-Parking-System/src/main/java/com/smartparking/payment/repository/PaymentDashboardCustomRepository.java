package com.smartparking.payment.repository;

import com.smartparking.payment.dto.response.dashboard.*;
import com.smartparking.payment.entity.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PaymentDashboardCustomRepository {

    PaymentKpiResponse getPaymentKpis(Specification<Payment> spec);

    List<PaymentMethodShareResponse> getMethodShares(Specification<Payment> spec);

    List<TopCustomerRevenueResponse> getTopCustomers(Specification<Payment> spec, int limit);
    List<RevenueTimeSeriesResponse> getRevenueTimeSeries(Specification<Payment> spec);
}