package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "groups_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupsProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "profile_code", length = 20, unique = true)
    private String profileCode;

    @Column(name = "profile_name", length = 50)
    private String profileName;
}
