package com.smartparking.identity.controller.admin;

import com.smartparking.identity.entity.Role;
import com.smartparking.identity.service.admin.AdminRoleService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> listRoles(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getAllRoles(search)));
    }
}
