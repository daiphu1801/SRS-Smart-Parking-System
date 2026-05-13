package com.smartparking.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintCreateRequest {
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    private String imgUrl; // Có thể null nếu không đính kèm ảnh
}