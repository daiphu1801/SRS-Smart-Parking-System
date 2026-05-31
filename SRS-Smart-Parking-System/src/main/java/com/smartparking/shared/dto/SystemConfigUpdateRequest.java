package com.smartparking.shared.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SystemConfigUpdateRequest {
    @NotBlank(message = "Giá trị cấu hình không được để trống")
    private String configValue;

    private String description; // Cho phép Admin cập nhật lại giải thích nếu cần
}