package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "account_id", unique = true)
    private Integer accountId;
    @Column(name = "group_id")
    private Integer groupId;
    @Column(name = "full_name", length = 100)
    private String fullName;
    @Column(length = 15, unique = true)
    private String phone;
    @Column(length = 255)
    private String address;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
