package com.smartparking.payment.service;

import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.dashboard.*;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.specification.PaymentSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardPaymentService {

    // Nơi đây chứa cả các hàm JpaRepository chuẩn và 3 hàm thống kê custom
    private final PaymentRepository paymentRepository;

    // Hàm lắp ráp Specification
    private Specification<Payment> buildDashboardSpec(PaymentFilterRequest filter) {
        return Specification.where(PaymentSpecs.isNotTrash()) // Mặc định bỏ rác
                .and(PaymentSpecs.hasStatus(filter.getPayStatus()))
                .and(PaymentSpecs.hasMethod(filter.getMethod()))
                .and(PaymentSpecs.hasGateway(filter.getGateway()))
                .and(PaymentSpecs.hasCreatedAtBetween(filter.getCreatedAtFrom(), filter.getCreatedAtTo()));
    }

    public PaymentKpiResponse getPaymentKpis(PaymentFilterRequest filter) {
        Specification<Payment> spec = buildDashboardSpec(filter);
        return paymentRepository.getPaymentKpis(spec);
    }

    public List<PaymentMethodShareResponse> getMethodShares(PaymentFilterRequest filter) {
        Specification<Payment> spec = buildDashboardSpec(filter);
        List<PaymentMethodShareResponse> results = paymentRepository.getMethodShares(spec);

        // Tính % trên Java để tránh phức tạp hóa câu SQL
        BigDecimal totalAmount = results.stream()
                .map(PaymentMethodShareResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            results.forEach(item -> {
                double percent = item.getTotalAmount()
                        .multiply(new BigDecimal(100))
                        .divide(totalAmount, 2, RoundingMode.HALF_UP)
                        .doubleValue();
                item.setPercentage(percent);
            });
        }
        return results;
    }

    public List<TopCustomerRevenueResponse> getTopCustomers(PaymentFilterRequest filter, int limit) {
        Specification<Payment> spec = buildDashboardSpec(filter);
        return paymentRepository.getTopCustomers(spec, limit);
    }

    public List<RevenueTimeSeriesResponse> getRevenueTimeSeries(PaymentFilterRequest filter) {
        Specification<Payment> spec = buildDashboardSpec(filter);
        return paymentRepository.getRevenueTimeSeries(spec);
    }
}