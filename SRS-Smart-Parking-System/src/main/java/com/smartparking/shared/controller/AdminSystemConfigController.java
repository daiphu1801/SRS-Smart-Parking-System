package com.smartparking.shared.controller;


import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.SystemConfigResponse;
import com.smartparking.shared.dto.SystemConfigUpdateRequest;
import com.smartparking.shared.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/system-configs")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    // 1. LẤY TẤT CẢ CẤU HÌNH (Phục vụ màn hình quản trị)
    @PreAuthorize("hasAuthority('CONFIG_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<com.smartparking.shared.dto.SystemConfigResponse>>> getAllConfigs() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách cấu hình hệ thống thành công",
                systemConfigService.getAllConfigs()
        ));
    }

    @PreAuthorize("hasAuthority('CONFIG_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(
            @PathVariable Integer id,
            @Valid @RequestBody SystemConfigUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật cấu hình hệ thống thành công",
                systemConfigService.updateConfig(id, request)
        ));
    }
}