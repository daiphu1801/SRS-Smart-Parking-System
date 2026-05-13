package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "function_code", length = 50)
    private String functionCode;

    @Column(length = 255)
    private String description;
}
