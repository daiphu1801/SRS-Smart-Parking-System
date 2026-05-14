package com.smartparking.identity.dto.response;

import com.smartparking.identity.entity.GroupsCustomer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private Integer id;
    private Integer groupId;
    private Integer accountId;
    private String fullName;
    private String phone;
    private Boolean isOnline;
    private String address;
    private LocalDateTime createdAt;
    private String groupName;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
