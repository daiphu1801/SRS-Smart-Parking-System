package com.smartparking.identity.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String role;
    private String accountType;
    private Integer accountId;
    private String fullName;
}
