package com.smartparking.payment.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTimeSeriesResponse {
    private String timeLabel;       // Nhãn trục X (VD: "2026-05-01", "08:00")
    private BigDecimal revenue;     // Tiền thu được
    private Long transactionCount;  // Lượt giao dịch
}