package com.smartparking.identity.dto.request;

import lombok.Data;

@Data
public class EmployeeCreateRequest {
    private String fullName;
    private String phone;
    private Integer createdBy;
    private String password;
    private String accoutType;// Optional
    private int roleId;

}
