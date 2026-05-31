package com.smartparking.identity.entity; // Đổi lại package của bạn cho đúng

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_one_time")
@Getter
@Setter
public class OtpOneTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nối với ID của bảng Account trong local
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_used")
    private boolean isUsed = false;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "otp_type")
    private OtpType type;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "phone", updatable = false)
    private String phone;
    @Column(name = "try_time")
    private Integer tryTime;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}