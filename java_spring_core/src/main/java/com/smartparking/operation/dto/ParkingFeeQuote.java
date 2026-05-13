package com.smartparking.operation.dto;

import com.smartparking.operation.entity.DayType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ParkingFeeQuote {
    private Long sessionId;
    private String plateNumber;
    private Integer vehicleTypeId;
    private LocalDateTime entryTime;
    private LocalDateTime calcTime;
    private Long durationMinutes;
    private BigDecimal amount;
    private DayType dayType;
    private Boolean paid;
    private Boolean subscription;
}
