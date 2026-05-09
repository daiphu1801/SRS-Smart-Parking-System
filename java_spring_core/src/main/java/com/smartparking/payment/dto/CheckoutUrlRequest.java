package com.smartparking.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutUrlRequest {
    private String targetType; // SESSION | BOOKING_DETAIL
    private Long targetId;
    private String method; // VNPAY | PAYOS
    private BigDecimal amount;
}
