package com.smartparking.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "package_price")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "pkg_veh_type_id")
    private Integer pkgVehTypeId;
    @Column(name = "duration_months")
    private Integer durationMonths;
    @Column(precision = 15, scale = 2)
    private BigDecimal price;
    @Builder.Default
    private Boolean isActive = true;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
