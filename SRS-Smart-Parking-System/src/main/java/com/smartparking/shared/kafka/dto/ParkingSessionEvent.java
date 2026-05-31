package com.smartparking.shared.kafka.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSessionEvent {
    private Long id;
    private String vehicleNo;
    private String entryTime;
    private String exitTime;
    private String gracePeriodEnd;
    private Integer bookingDetailId;
    private Integer vehicleTypeId;
    private Integer zoneInId;
    private Integer zoneOutId;
    private String imageInUrl;
    private String imageOutUrl;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal amountLeft;
    private Boolean flagManual;
}