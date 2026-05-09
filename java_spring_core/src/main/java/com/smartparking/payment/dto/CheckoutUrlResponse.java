package com.smartparking.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CheckoutUrlResponse {
    private String payCode;
    private String checkoutUrl;
    private BigDecimal amount;
    private String method;
    private String status;
}
