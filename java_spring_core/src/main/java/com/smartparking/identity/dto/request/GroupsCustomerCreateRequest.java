package com.smartparking.identity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupsCustomerCreateRequest {
    private String groupName;
    private String groupCode;
    @NotNull(message = "Profile ID không được để trống")
    private Integer profileId;
    private Integer masterAccountId;
    private Integer createdBy;
    private LocalDateTime createdAt;

}
