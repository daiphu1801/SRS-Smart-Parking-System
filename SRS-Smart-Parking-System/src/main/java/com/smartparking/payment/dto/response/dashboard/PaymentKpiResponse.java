package com.smartparking.payment.dto.response.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentKpiResponse {
    private BigDecimal totalRevenue;       // Tổng doanh thu
    private Long totalTransactions;        // Tổng số giao dịch
    private Long successfulTransactions;   // Số giao dịch thành công
    private Long failedTransactions;       // Số giao dịch xịt/hủy
}