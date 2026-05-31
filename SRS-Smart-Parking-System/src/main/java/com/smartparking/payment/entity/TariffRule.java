package com.smartparking.payment.entity;

import com.smartparking.operation.entity.DayType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "tariff_rules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TariffRule  {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;
    @Column(name = "is_active",columnDefinition = "boolean default true")
    private Boolean isActive = true;
}
