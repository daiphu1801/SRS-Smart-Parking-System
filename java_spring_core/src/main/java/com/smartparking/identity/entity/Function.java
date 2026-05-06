package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "functions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "func_name", length = 50)
    private String funcName;

    @Column(length = 255)
    private String description;
}
