package com.smartparking.payment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentCheckoutResponse {

    private Long paymentId;
    private String paymentCode;
    private BigDecimal amount;
    private String checkoutUrl;

    private String message;
}