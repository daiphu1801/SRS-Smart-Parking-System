package com.smartparking.operation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zones")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Zone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(name = "parent_zone_id") private Integer parentZoneId;
    @Column(name = "zone_name", length = 100) private String zoneName;
    @Enumerated(EnumType.STRING) @Column(name = "zone_type") private ZoneType zoneType;
    private Integer capacity;
    @Column(name = "current_occupancy") @Builder.Default private Integer currentOccupancy = 0;
}
