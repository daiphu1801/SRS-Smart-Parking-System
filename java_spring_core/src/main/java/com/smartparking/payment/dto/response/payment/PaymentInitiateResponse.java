package com.smartparking.payment.dto.response.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentInitiateResponse {
    private String payCode;         // Mã giao dịch nội bộ của ông (VD: SP-20260510-1234)
    private BigDecimal amount;      // Tổng tiền để FE hiển thị lại cho chắc
    private Long paymentId;
    private String paymentCode;

    private String checkoutUrl;

    private String message;
}