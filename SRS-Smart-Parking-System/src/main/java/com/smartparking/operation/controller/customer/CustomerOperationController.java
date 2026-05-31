package com.smartparking.operation.controller.customer;

import com.smartparking.operation.dto.request.DeleteDraftsRequest;
import com.smartparking.operation.dto.request.RenewalBookingRequest;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.shared.dto.CustomAccountPrincipal;
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

    @GetMapping("/booking-details")
    public ResponseEntity<ApiResponse<List<BookingDetailDto>>> getBookingDetails(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @RequestParam(required = false) List<BookingStatus> statuses)
    {

        Integer id = principal.getMemberGroupIds().getFirst();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách xe thành công",
                bookingService.getBookingDetailsByStatus(id, statuses)
        ));
    }

    @GetMapping("/booking-details/{detailId}")
    public ResponseEntity<ApiResponse<BookingDetailDto>> getBookingDetailById(
            @PathVariable Integer detailId,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer groupId = principal.getMemberGroupIds().getFirst();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin chi tiết xe thành công",
                bookingService.getBookingDetailByIdAndGroupId(detailId, groupId) // Truyền cả 2 xuống
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
    public ResponseEntity<ApiResponse<Void>> deleteSelectedDrafts(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @Valid @RequestBody DeleteDraftsRequest request) { // Nhận list ID từ Frontend

        Integer customerId = principal.getCustomerId();
        Integer groupId = principal.getMemberGroupIds().getFirst();

        // Truyền thêm cái list draftIds xuống cho Service xử lý
        bookingDetailService.deleteSelectedDrafts(customerId, groupId, request.getDraftIds());

        return ResponseEntity.ok(ApiResponse.success("Đã hủy các xe được chọn khỏi giỏ hàng", null));
    }

    @PostMapping("/renew_booking")
    public ResponseEntity<ApiResponse<List<BookingDetailDto>>> renewBooking(
            @RequestBody RenewalBookingRequest request,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {


        List<Integer> myGroupIds = principal.getMemberGroupIds();

        return ResponseEntity.ok(ApiResponse.success(
                "Khởi tạo thanh toán gia hạn thành công",
                bookingDetailService.createRenewalDrafts(request,myGroupIds)
        ));
    }
}
