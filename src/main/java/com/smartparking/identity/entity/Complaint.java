package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "solved_by")
    private Integer solvedBy;
    @Column(name = "content")
    private String content;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "solved_at")
    private LocalDateTime solvedAt;
    @Column(name = "is_solved")
    private Boolean isSolved;

    @Column(name = "img_url")
    private String imgUrl;

}
