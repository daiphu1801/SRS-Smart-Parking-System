package com.smartparking.operation.controller.admin;

import com.smartparking.operation.dto.request.BookingCreateRequest;
import com.smartparking.operation.dto.response.BookingAndDetailResponse;
import com.smartparking.operation.dto.response.BookingResponse;
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
@RequestMapping("/api/v1/employee/bookings")
@RequiredArgsConstructor
public class EmployeeBookingController {
    private final BookingService bookingService;

    // 1. LẤY DANH SÁCH (Phân trang + Lọc + Join đầy đủ Tên Nhóm, Tên Gói)
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> listBookings(
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Integer packageId,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách Booking thành công",
                bookingService.getAllBookings(groupId, packageId, pageable)
        ));
    }

    // 2. CHI TIẾT 1 BOOKING
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết Booking thành công",
                bookingService.getBookingDetail(id)
        ));
    }

    // 3. TẠO MỚI (Trường hợp Admin tự tay đăng ký/bán gói cho Khách hàng tại quầy)
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingCreateRequest request) {

        // createdBy sẽ được Service móc từ Token của Admin ra
        return ResponseEntity.status(201).body(ApiResponse.success(
                "Tạo Booking thành công",
                bookingService.createBooking(request)
        ));
    }

    // 4. CẬP NHẬT (Admin sửa sai: Ví dụ khách muốn đổi từ gói Tháng sang gói Năm)
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Integer id,
            @Valid @RequestBody BookingCreateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật Booking thành công",
                bookingService.updateBooking(id, request)
        ));
    }

    // 5. XÓA / HỦY BOOKING (Dọn dẹp data hoặc khách trả lại gói)
    @PreAuthorize("hasAuthority('BOOKING_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Integer id) {
        bookingService.deleteBooking(id);

        return ResponseEntity.ok(ApiResponse.success(
                "Xóa Booking thành công",
                null
        ));
    }
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<BookingAndDetailResponse>> getBookingAndDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết Hợp đồng và Danh sách xe thành công",
                bookingService.getBookingAndDetails(id)
        ));
    }
}
