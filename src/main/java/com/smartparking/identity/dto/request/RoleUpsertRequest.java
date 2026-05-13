package com.smartparking.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpsertRequest {

    @NotBlank(message = "Tên Role không được để trống")
    private String roleName;

    private String description;

    // Danh sách quyền Frontend ném lên
    private List<PermissionRequest> permissions;

    @Data
    public static class PermissionRequest {
        @NotNull(message = "Function ID không được null")
        private Integer funcId;

        @NotNull(message = "Action ID không được null")
        private Integer actionId;
    }
}