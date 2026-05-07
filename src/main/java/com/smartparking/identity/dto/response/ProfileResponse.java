package com.smartparking.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    @JsonProperty("account_id")
    private Integer accountId;

    private String username;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("full_name")
    private String fullName;

    private String phone;

    private String email;
}
