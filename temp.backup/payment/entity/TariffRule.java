package com.smartparking.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "tariff_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;
    @Enumerated(EnumType.STRING)
    @Column(name = "day_type")
    private DayType dayType;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "base_block_mins")
    private Integer baseBlockMins;
    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;
    @Column(name = "next_block_mins")
    private Integer nextBlockMins;
    @Column(name = "next_block_price", precision = 15, scale = 2)
    private BigDecimal nextBlockPrice;
    @Column(name = "max_price_per_day", precision = 15, scale = 2)
    private BigDecimal maxPricePerDay;
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
