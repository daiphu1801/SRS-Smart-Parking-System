package com.smartparking.payment.dto.response.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerRevenueResponse {
    private Integer customerId;
    private String customerPhone;
    private BigDecimal totalSpent; // Tổng tiền đã đốt vào bãi xe
    private Long ticketCount;      // Số lần gửi xe
}