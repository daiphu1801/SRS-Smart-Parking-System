package com.smartparking.identity.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Complaint extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "solved_by")
    private Integer solvedBy;
    @Column(name = "content")
    private String content;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;
    @Column(name = "is_solved")
    private Boolean isSolved;

    @Column(name = "img_url")
    private String imgUrl;

}
