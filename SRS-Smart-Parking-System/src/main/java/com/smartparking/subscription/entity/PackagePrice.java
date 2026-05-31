package com.smartparking.subscription.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "package_price")
@Getter    // Lấy biến
@Setter    // Sửa biến
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class PackagePrice extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "pkg_veh_type_id")
    private Integer pkgVehTypeId;
    @Column(name = "duration_months")
    private Integer durationMonths;
    @Column(precision = 15, scale = 2)
    private BigDecimal price;
    @Column(name = "package_price_name")
    private String packagePriceName;

    @Builder.Default
    private Boolean isActive = true;

}
