package com.smartparking.operation.controller.customer;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.BookingDetailCreateRequest;
import com.smartparking.operation.dto.response.BookingAndDetailResponse;
import com.smartparking.operation.service.admin.BookingDetailService;
import com.smartparking.operation.service.admin.BookingService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/operation")
@RequiredArgsConstructor
public class CustomerOperationController {
    private final BookingService bookingService;
    private final BookingDetailService bookingDetailService;

    @GetMapping("/booking")
    public ResponseEntity<ApiResponse<BookingAndDetailResponse>> getBookingAndDetails(
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer id = principal.getMemberGroupIds().getFirst();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết Hợp đồng và Danh sách xe thành công",
                bookingService.getBookingAndDetails(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDetailDto>> createBookingDetail(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @Valid @RequestBody BookingDetailCreateRequest request) {
        Integer customerId = principal.getCustomerId();
        Integer groupId = principal.getMemberGroupIds().getFirst();
        return ResponseEntity.status(201).body(ApiResponse.success(
                "Tạo BookingDetail thành công",
                bookingDetailService.createBookingDetailDraft(customerId,groupId,request)
        ));
    }

    @GetMapping("/drafts")
    public ResponseEntity<ApiResponse<List<BookingDetailDto>>> getDraftBookingDetails(
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer groupId = principal.getMemberGroupIds().getFirst();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách xe chờ xác nhận",
                bookingDetailService.getDraftBookingDetails( groupId)
        ));
    }

    @DeleteMapping("/drafts")
    public ResponseEntity<ApiResponse<Void>> clearAllDrafts(
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer customerId = principal.getCustomerId();
        Integer groupId = principal.getMemberGroupIds().getFirst();

        bookingDetailService.clearAllDrafts(customerId, groupId);

        return ResponseEntity.ok(ApiResponse.success("Đã hủy toàn bộ xe trong giỏ hàng", null));
    }

}
