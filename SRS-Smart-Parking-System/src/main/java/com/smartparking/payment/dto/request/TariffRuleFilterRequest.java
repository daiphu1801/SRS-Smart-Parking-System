package com.smartparking.payment.dto.request;

import com.smartparking.operation.entity.DayType;
import lombok.Data;

@Data
public class TariffRuleFilterRequest {
    private Integer vehicleTypeId;
    private DayType dayType;
    private Boolean isActive = true;
}
