package com.smartparking.identity.controller.customer;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.identity.dto.request.ComplaintCreateRequest;
import com.smartparking.identity.dto.response.ComplaintDetailResponse;
import com.smartparking.identity.service.ComplaintService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/complaints")
@RequiredArgsConstructor
public class CustomerComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> createComplaint(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @Valid @RequestBody ComplaintCreateRequest request) {

        // Lấy ID khách hàng từ Token
        Integer customerId = principal.getCustomerId();

        return ResponseEntity.status(201).body(ApiResponse.success(
                "Gửi khiếu nại thành công! Chúng tôi sẽ xử lý sớm nhất.",
                complaintService.createComplaint(customerId, request)
        ));
    }
}