package com.smartparking.identity.dto.request;

import lombok.Data;

@Data
public class AccountUpdateRequest {
    private Integer roleId;
    private String accountType;
    private String status;
    private String phone;
    private String passwordHash;
}
