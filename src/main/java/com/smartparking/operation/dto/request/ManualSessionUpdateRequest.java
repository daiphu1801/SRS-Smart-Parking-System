package com.smartparking.operation.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualSessionUpdateRequest {
    // Sửa biển số nếu AI nhìn nhầm
    private String correctVehicleNo;

    // Admin/Kế toán can thiệp dòng tiền nếu có sự cố (VD: Khách phàn nàn, thu tiền mặt ngoài lề)
    private BigDecimal updateAmountPaid;
    private Integer updateVehicleTypeId;
}