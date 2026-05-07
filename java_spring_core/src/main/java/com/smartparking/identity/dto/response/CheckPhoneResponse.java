package com.smartparking.identity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckPhoneResponse {
    private String phone;
    private String action; // Có 2 giá trị: REQUIRE_LOGIN_PASSWORD hoặc REQUIRE_CREATE_PASSWORD
}