package com.smartparking.payment.dto.request;

import com.smartparking.operation.entity.DayType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class TariffRuleCreateRequest {
    @NotNull(message = "Loại xe không được để trống")
    private Integer vehicleTypeId;

    @NotNull(message = "Loại ngày không được để trống")
    private DayType dayType;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalTime endTime;

    @NotNull(message = "Giá cơ bản không được để trống")
    private BigDecimal basePrice;

    private Boolean isActive = true;
}