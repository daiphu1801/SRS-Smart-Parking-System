package com.smartparking.identity.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintFilterRequest {
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private Integer createdBy; // Lọc theo ID khách
    private Integer solvedBy;  // Lọc theo ID nhân viên
    private Boolean isSolved;  // Lọc trạng thái (true/false)
}