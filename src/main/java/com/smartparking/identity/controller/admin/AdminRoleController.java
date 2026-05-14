package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.request.RoleUpsertRequest;
import com.smartparking.identity.dto.response.RoleDetailResponse;
import com.smartparking.identity.dto.response.SystemFunctionActionResponse;
import com.smartparking.identity.entity.Role;
import com.smartparking.identity.service.admin.AdminRoleService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService roleService;
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> listRoles(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getAllRoles(search)));
    }
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/functions-actions")
    public ResponseEntity<ApiResponse<SystemFunctionActionResponse>> getAllFunctionAndAction() {
        SystemFunctionActionResponse data = roleService.getAllFunctionAndAction();
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getRoleDetail(@PathVariable("id") Integer id) {
        RoleDetailResponse data = roleService.getRoleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(@Valid @RequestBody RoleUpsertRequest request) {
        Role createdRole = roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success(createdRole));
    }

    // 5. API CẬP NHẬT ROLE VÀ QUYỀN
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(
            @PathVariable("id") Integer id,
            @Valid @RequestBody RoleUpsertRequest request) {
        Role updatedRole = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedRole));
    }

    // 6. API XÓA MỀM ROLE
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable("id") Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa Role thành công"));
    }
}
