package com.smartparking.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartparking.identity.entity.RoleFunctionAction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthLoginResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("account_type")
    private String accountType;
    
    @JsonProperty("account_id")
    private Integer accountId;

    @JsonProperty("permissions")
    List<String> permissions;
}
