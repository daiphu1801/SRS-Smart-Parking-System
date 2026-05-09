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

    // Mapping Enum với kiểu String trong DB (hoặc có thể dùng custom converter nếu dùng Postgres Enum)
    // Để Spring Boot tự động mapping Enum của Java với Enum của Postgres, cấu hình như sau:
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "otp_type")
    private OtpType type;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}