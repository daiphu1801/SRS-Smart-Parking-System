package com.smartparking.kafka.dto;

import lombok.Data;

/** JSON payload từ Python Edge (vehicle-entry / vehicle-exit topics) */
@Data
public class VehicleEvent {
    private String plate;
    private Integer vehicleTypeId;
    private Integer zoneId;
    private String deviceId;
    private String imageUrl;
    private long timestamp;
}
