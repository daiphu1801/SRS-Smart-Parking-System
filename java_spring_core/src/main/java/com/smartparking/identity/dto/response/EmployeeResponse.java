package com.smartparking.identity.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponse {
    private Integer id;
    private Integer accountId;
    private String fullName;
    private String phone;
    private String email;
    private Boolean isOnline;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
