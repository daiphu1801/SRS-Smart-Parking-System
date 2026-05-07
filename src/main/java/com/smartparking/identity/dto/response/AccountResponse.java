package com.smartparking.identity.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Integer id;
    private String username;
    private Integer roleId;
    private String roleName;
    private String accountType;
    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
