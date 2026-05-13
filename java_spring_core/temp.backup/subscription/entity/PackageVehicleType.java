package com.smartparking.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "package_vehicle_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageVehicleType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "profile_id")
    private Integer profileId;

    @Column(name = "package_id")
    private Integer packageId;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "max_quantity")
    private Integer maxQuantity;
}
