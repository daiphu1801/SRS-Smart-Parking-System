package com.smartparking.operation.dto.request;

import lombok.Data;

@Data
public class IotExitRequest {
    private String vehicleNo;
    private Integer zoneId;
    private String imageUrl;
}
