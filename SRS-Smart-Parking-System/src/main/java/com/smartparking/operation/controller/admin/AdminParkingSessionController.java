package com.smartparking.operation.controller.admin;

import com.smartparking.operation.dto.request.ManualSessionUpdateRequest;
import com.smartparking.operation.dto.request.ParkingSessionFilterRequest;
import com.smartparking.operation.dto.response.ParkingSessionResponse;
import com.smartparking.operation.service.admin.AdminParkingSessionService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/parking-sessions")
@RequiredArgsConstructor
public class AdminParkingSessionController {
    private final AdminParkingSessionService sessionService;
    @PreAuthorize("hasAuthority('SESSION_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ParkingSessionResponse>>> listSessions(
            @ModelAttribute ParkingSessionFilterRequest filter,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách phiên đỗ xe thành công",
                sessionService.getAllSessions(filter, pageable)));
    }

    @PreAuthorize("hasAuthority('SESSION_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> getSessionDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết phiên đỗ xe thành công",
                sessionService.getSessionDetail(id)));
    }

    // 3. SỬA THỦ CÔNG (Sửa biển số hoặc can thiệp tiền bạc)
    @PreAuthorize("hasAuthority('SESSION_OVERRIDE')")
    @PutMapping("/{id}/manual-update")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> updateSessionManually(
            @PathVariable Long id,
            @RequestBody ManualSessionUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật thủ công thành công, hệ thống đã ghi nhận log",
                sessionService.updateSessionManually(id, request)));
    }
}
