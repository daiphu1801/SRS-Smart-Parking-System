package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.identity.dto.request.ComplaintFilterRequest;
import com.smartparking.identity.dto.response.ComplaintDetailResponse;
import com.smartparking.identity.service.ComplaintService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComplaintDetailResponse>>> getAllComplaints(
            @ModelAttribute ComplaintFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách khiếu nại thành công",
                complaintService.getComplaintsWithFilter(filter, page, size)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> getComplaintById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết khiếu nại thành công",
                complaintService.getComplaintById(id)
        ));
    }

    @PutMapping("/{id}/solve")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> solveComplaint(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        // Lấy ID của Admin/Bảo vệ đang thao tác từ Token
        Integer employeeId = principal.getEmployeeId();

        return ResponseEntity.ok(ApiResponse.success(
                "Đã đánh dấu xử lý khiếu nại thành công!",
                complaintService.solveComplaint(id, employeeId)
        ));
    }
}