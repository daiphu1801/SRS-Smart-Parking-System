package com.smartparking.operation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequest {
    @NotNull(message = "Lỗi: Vui lòng chọn Nhóm (Group ID)!")
    private Integer groupId;

    @NotNull(message = "Lỗi: Vui lòng chọn Gói cước (Package ID)!")
    private Integer packageId;
}
