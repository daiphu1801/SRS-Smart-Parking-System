package com.smartparking.payment.dto.response.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDetailResponse {
    private Long id;
    private Long paymentId;
    private Integer bookingDetailId;

    private BigDecimal itemAmount;
    private LocalDateTime appliedStartDate;
    private LocalDateTime appliedEndDate;
}