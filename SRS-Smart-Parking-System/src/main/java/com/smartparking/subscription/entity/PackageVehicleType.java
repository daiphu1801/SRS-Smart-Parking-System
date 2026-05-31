package com.smartparking.subscription.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "package_vehicle_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class PackageVehicleType extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "package_id")
    private Integer packageId;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "max_quantity")
    private Integer maxQuantity;
}
