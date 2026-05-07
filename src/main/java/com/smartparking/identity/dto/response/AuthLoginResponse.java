package com.smartparking.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLoginResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("account_type")
    private String accountType;
    
    @JsonProperty("account_id")
    private Integer accountId;
}
