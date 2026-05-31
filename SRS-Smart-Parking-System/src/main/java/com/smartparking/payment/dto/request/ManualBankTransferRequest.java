package com.smartparking.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ManualBankTransferRequest {

    @NotBlank(message = "Biển số xe không được để trống")
    private String vehicleNo;

    @NotNull(message = "Số tiền xác nhận không được để trống")
    @DecimalMin(value = "1.0", message = "Số tiền xác nhận phải lớn hơn 0")
    private BigDecimal amount;

    private String note;
}