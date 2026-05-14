package com.smartparking.operation.controller.customer;

import com.smartparking.operation.dto.request.ManualSessionUpdateRequest;
import com.smartparking.operation.dto.request.ParkingSessionFilterRequest;
import com.smartparking.operation.dto.response.ParkingSessionResponse;
import com.smartparking.operation.service.admin.AdminParkingSessionService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/parking-sessions")
@RequiredArgsConstructor
public class CustomerParkingSessionController {
    private final AdminParkingSessionService sessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ParkingSessionResponse>>> listSessions(
            @ModelAttribute ParkingSessionFilterRequest filter,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách phiên đỗ xe thành công",
                sessionService.getAllSessions(filter, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> getSessionDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết phiên đỗ xe thành công",
                sessionService.getSessionDetail(id)
        ));
    }

}
