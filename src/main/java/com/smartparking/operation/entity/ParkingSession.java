package com.smartparking.operation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartparking.subscription.entity.VehicleType;

@Entity
@Table(name = "parking_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_detail_id")
    private Integer bookingDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_detail_id", insertable = false, updatable = false)
    private BookingDetail bookingDetail;

    @Column(name = "vehicle_no", length = 20, nullable = false)
    private String vehicleNo;

    @Column(name = "vehicle_type_id", insertable = false, updatable = false)
    private Integer vehicleTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id")
    private VehicleType vehicleType;

    @Column(name = "zone_in_id", insertable = false, updatable = false)
    private Integer zoneInId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_in_id")
    private Zone zoneIn;

    @Column(name = "zone_out_id", insertable = false, updatable = false)
    private Integer zoneOutId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_out_id")
    private Zone zoneOut;
    @Column(name = "entry_time")
    private LocalDateTime entryTime;
    @Column(name = "exit_time")
    private LocalDateTime exitTime;
    @Column(name = "image_in_url", length = 255)
    private String imageInUrl;
    @Column(name = "image_out_url", length = 255)
    private String imageOutUrl;
    @Column(name = "grace_period_end")
    private LocalDateTime gracePeriodEnd;
    @Column(name = "amount_paid", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;
    @Column(name = "amount_left", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountLeft = BigDecimal.ZERO;
    @Column(name = "amount_due", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountDue = BigDecimal.ZERO;
    @Column(name = "flag_manual")
    @Builder.Default
    private Boolean flagManual = false;
}
