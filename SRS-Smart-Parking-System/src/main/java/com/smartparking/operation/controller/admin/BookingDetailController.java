package com.smartparking.operation.controller.admin;

import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.BookingCreateRequest;
import com.smartparking.operation.dto.request.BookingDetailCreateRequest;

import com.smartparking.operation.dto.response.BookingResponse;
import com.smartparking.operation.service.admin.BookingDetailService;
import com.smartparking.operation.service.admin.BookingService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/booking-details")
@RequiredArgsConstructor
public class BookingDetailController {
    private final BookingDetailService bookingDetailService;
    private final BookingService bookingService;
    // 1. LẤY DANH SÁCH (Phân trang + Lọc + Join đầy đủ Tên Nhóm, Tên Gói)
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingDetailDto>>> listBookingDetails(
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Integer packageId,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách BookingDetail thành công",
                bookingDetailService.getAllBookingDetail(groupId, packageId, pageable)
        ));
    }

    // 2. CHI TIẾT 1 BookingDetail
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailDto>> getBookingDetailDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết BookingDetail thành công",
                bookingDetailService.getBookingDetailById(id)
        ));
    }

    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingDetailDto>> createBookingDetail(
            @Valid @RequestBody BookingDetailCreateRequest request) {

        return ResponseEntity.status(201).body(ApiResponse.success(
                "Tạo BookingDetail thành công",
                bookingDetailService.createBookingDetail(request)
        ));
    }
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailDto>> updateBookingDetail(
            @PathVariable Integer id,
            @Valid @RequestBody BookingDetailCreateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật BookingDetail thành công",
                bookingDetailService.updateBookingDetail(id, request)
        ));
    }

    // 5. XÓA / HỦY BookingDetail (Dọn dẹp data hoặc khách trả lại gói)
    @PreAuthorize("hasAuthority('BOOKING_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBookingDetail(@PathVariable Integer id) {
        bookingDetailService.deleteBookingDetail(id);

        return ResponseEntity.ok(ApiResponse.success(
                "Xóa BookingDetail thành công",
                null
        ));
    }




}
