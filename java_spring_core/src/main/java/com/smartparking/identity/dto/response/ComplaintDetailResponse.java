package com.smartparking.identity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintDetailResponse {
    private Integer id;
    private String content;
    private String imgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime solvedAt;
    private Boolean isSolved;

    // --- Thông tin Customer (Người tạo) ---
    private Integer createdBy;
    private String customerName;
    private String customerPhone;

    // --- Thông tin Employee (Người xử lý) ---
    private Integer solvedBy;
    private String employeeName;
    private String employeePhone;
}