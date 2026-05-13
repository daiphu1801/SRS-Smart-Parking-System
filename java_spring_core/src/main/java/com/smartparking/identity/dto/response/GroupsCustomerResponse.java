package com.smartparking.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupsCustomerResponse {
    private Integer id;
    private String groupName;
    private String groupCode;
    private Integer profileId;
    private Integer masterAccountId;
    private Integer createdBy;
    private LocalDateTime createdDate;

    private String masterPhone;
    private String profileCode;
    private String profileName;


    }
