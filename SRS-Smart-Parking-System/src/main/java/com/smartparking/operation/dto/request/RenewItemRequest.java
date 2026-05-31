package com.smartparking.operation.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewItemRequest {

    @NotNull(message = "Thiếu ID của hợp đồng xe cũ!")
    private Integer oldBookingDetailId;

    @NotNull(message = "Vui lòng chọn gói cước mới để gia hạn!")
    private Integer newPackagePriceId;

}