package com.smartparking.operation.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponse {
    private Integer id;
    private LocalDateTime createdAt;

    // Thông tin Join từ CustomerGroup
    private Integer groupId;
    private String groupName;
    private String groupCode;

    // Thông tin Join từ Package
    private Integer packageId;
    private String packageName;

    // Thông tin người tạo (từ Account/Customer)
    private Integer createdBy;
    private String creatorName;
}
