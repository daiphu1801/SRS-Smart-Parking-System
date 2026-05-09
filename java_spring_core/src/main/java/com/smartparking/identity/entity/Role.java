package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role_name", length = 50, unique = true)
    private String roleName;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive;
}
