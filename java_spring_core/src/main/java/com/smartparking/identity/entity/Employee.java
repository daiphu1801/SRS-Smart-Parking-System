package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
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
    @Column(name = "created_by")
    private Integer createdBy;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
