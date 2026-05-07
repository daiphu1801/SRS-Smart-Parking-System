package com.smartparking.identity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerCreateRequest {
    private String fullName;
    private String phone;
    private Integer createdBy;
    private String accoutType;// Optional
    private int roleId;
    @NotNull(message = "Group    ID không được để trống")
    private int groupId;
    private String address;
}
