package com.smartparking.identity.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Employee extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "account_id", unique = true)
    private Integer accountId;
    @Column(name = "full_name", length = 100)
    private String fullName;
    @Column(length = 15, unique = true)
    private String phone;
    @Column(name = "is_online")
    @Builder.Default
    private Boolean isOnline = false;
    @Column(name = "deleted")
    private Boolean deleted;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
