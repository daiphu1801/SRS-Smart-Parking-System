package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "groups_customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupsCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "profile_id")
    private Integer profileId;
    @Column(name = "group_code", unique = true, length = 50)
    private String groupCode;
    @Column(name = "group_name", length = 100)
    private String groupName;
    @Column(name = "master_account_id")
    private Integer masterAccountId;
    @Column(name = "created_by")
    private Integer createdBy;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
