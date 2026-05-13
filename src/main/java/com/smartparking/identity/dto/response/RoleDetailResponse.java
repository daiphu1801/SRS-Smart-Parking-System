package com.smartparking.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoleDetailResponse {
    private Integer roleId;
    private String roleName;
    private String description;

    // Trả về mảng các cặp [funcId, actionId] để UI biết ô nào cần Tích xanh (Checked)
    private List<RolePermissionDto> permissions;

    @Data @Builder
    public static class RolePermissionDto {
        private Integer funcId;
        private Integer actionId;
        private String permissionCode;
    }
}