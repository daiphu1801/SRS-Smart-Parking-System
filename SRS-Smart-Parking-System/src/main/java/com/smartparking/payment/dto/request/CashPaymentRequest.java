package com.smartparking.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashPaymentRequest {

    @NotNull(message = "Mã cuộc xe (sessionId) không được để trống")
    private String vehicleNo;

    @NotNull(message = "Số tiền thu không được để trống")
    @Positive(message = "Số tiền thu phải lớn hơn 0")
    private BigDecimal amount;

}