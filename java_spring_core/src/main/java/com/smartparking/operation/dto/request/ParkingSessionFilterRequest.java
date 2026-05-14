package com.smartparking.operation.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkingSessionFilterRequest {
    private String vehicleNo;
    private Boolean flagManual;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime entryTimeFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime entryTimeTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime exitTimeFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime exitTimeTo;

    private Integer bookingDetailId;
    private Integer vehicleTypeId;
    private Integer zoneInId;
    private Integer zoneOutId;

    private BigDecimal paidGreaterThan;
    private BigDecimal paidLessThan;
}
