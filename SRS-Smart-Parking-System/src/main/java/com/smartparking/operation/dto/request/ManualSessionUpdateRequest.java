package com.smartparking.operation.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualSessionUpdateRequest {
    private String correctVehicleNo;

    private BigDecimal updateAmountPaid;
    private Integer updateVehicleTypeId;
}