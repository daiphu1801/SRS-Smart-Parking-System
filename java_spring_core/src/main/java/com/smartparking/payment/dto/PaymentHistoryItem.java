package com.smartparking.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentHistoryItem {
    private Long paymentId;
    private String payCode;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime createdAt;

    private Long paymentDetailId;
    private Integer bookingDetailId;
    private Long parkingSessionId;
    private BigDecimal itemAmount;
    private LocalDateTime appliedStartDate;
    private LocalDateTime appliedEndDate;
}
