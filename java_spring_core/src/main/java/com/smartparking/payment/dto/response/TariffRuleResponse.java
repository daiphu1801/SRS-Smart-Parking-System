package com.smartparking.payment.dto.response;

import com.smartparking.operation.entity.DayType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
public class TariffRuleResponse {
    private Integer id;
    private Integer vehicleTypeId;
    private DayType dayType;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal basePrice;
    private Boolean isActive;
}