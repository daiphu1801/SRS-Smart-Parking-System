package com.smartparking.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Integer accountId;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "ntext")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotificationType type;

    @Column(name = "is_read", columnDefinition = "boolean default false")
    private Boolean isRead;

    @Column(name = "reference_id")
    private Long referenceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
