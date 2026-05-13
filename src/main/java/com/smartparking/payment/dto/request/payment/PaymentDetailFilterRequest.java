package com.smartparking.payment.dto.request.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDetailFilterRequest {
    private Long paymentId;
    private Integer bookingDetailId;


    private BigDecimal minItemAmount;
    private BigDecimal maxItemAmount;

    private LocalDateTime appliedStartDateFrom;
    private LocalDateTime appliedStartDateTo;

    private LocalDateTime appliedEndDateFrom;
    private LocalDateTime appliedEndDateTo;
}
