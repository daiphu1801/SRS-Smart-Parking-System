package com.smartparking.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReconciliationResponse {
    private Long id;
    private String transactionId;
    private String payCode;
    private Long parkingSessionId;
    private BigDecimal amount;

    private String method;
    private String status;


    private String reconciliationFlag;
    private String warningMessage;
}