package com.smartparking.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_code", length = 20, unique = true)
    private String typeCode;

    @Column(name = "type_name", length = 50)
    private String typeName;
}
