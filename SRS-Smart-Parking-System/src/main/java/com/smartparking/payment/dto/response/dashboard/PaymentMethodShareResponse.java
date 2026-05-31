package com.smartparking.payment.dto.response.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodShareResponse {
    private String gatewayOrMethod; // VD: "VNPAY", "CASH", "MOMO"
    private BigDecimal totalAmount;
    private Double percentage;      // Tính sẵn % cho Frontend đỡ phải tự chia
}