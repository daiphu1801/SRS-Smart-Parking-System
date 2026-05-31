package com.smartparking.payment.dto.response.payment;

import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Integer payerId;

    private String customerFullName;
    private String customerPhone;
    private Long parkingSessionId;

    private String transactionId;
    private String payCode;
    private BigDecimal amount;
    private PaymentMethod method;
    private String gateway;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String checkoutUrl;
}
