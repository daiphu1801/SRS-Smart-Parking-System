package com.smartparking.operation.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IotEntryRequest {
    private String vehicleNo;
    private Integer vehicleTypeId;
    private Integer zoneId;
    private String imageUrl;
    private Integer deviceId;
}
