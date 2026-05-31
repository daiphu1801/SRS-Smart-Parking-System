package com.smartparking.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
    // Sau này ông có cấu hình thêm Multiple Database (1 DB đọc, 1 DB ghi)
    // thì cũng nhét hết vào file này, rất dễ quản lý!
}