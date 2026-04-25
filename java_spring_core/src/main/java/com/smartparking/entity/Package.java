package com.smartparking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packages")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Package {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "package_code", unique = true, length = 50) private String packageCode;
    @Column(name = "package_name", length = 100) private String packageName;
    @Column(length = 255) private String description;
}
