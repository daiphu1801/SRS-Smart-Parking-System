package com.smartparking.payment.controller.customer;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.payment.dto.request.CheckoutRequest;
import com.smartparking.payment.dto.request.payment.PaymentBookingRequest;
import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.PaymentCheckoutResponse;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.service.CustomerPaymentService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/customer/payments")
@RequiredArgsConstructor
public class CustomerPaymentController {

    private final CustomerPaymentService customerPaymentService;

    // 1. LẤY DANH SÁCH LỊCH SỬ GIAO DỊCH (Có lọc)
    @PostMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @ModelAttribute PaymentFilterRequest filter,
            Pageable pageable,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        List<Integer> myGroupIds = principal.getMemberGroupIds();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách lịch sử giao dịch",
                customerPaymentService.getMyPayments(myGroupIds, filter, pageable)
        ));
    }

    @PostMapping("/{id}/details")
    public ResponseEntity<ApiResponse<PaymentTreeResponse>> getMyPaymentDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        List<Integer> myGroupIds = principal.getMemberGroupIds();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết giao dịch thành công",
                customerPaymentService.getMyPaymentTreeDetails(id, myGroupIds)
        ));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        List<Integer> myGroupIds = principal.getMemberGroupIds();

        // Gọi sang Service để xử lý logic
        customerPaymentService.cancelPayment(id, myGroupIds);

        return ResponseEntity.ok(ApiResponse.success(
                "Đã hủy giao dịch thành công",
                null
        ));
    }

    @PostMapping("/booking")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiateBookingPayment(
            @RequestBody PaymentBookingRequest request,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {


        List<Integer> myGroupIds = principal.getMemberGroupIds();

        return ResponseEntity.ok(ApiResponse.success(
                "Khởi tạo thanh toán gia hạn thành công",
                customerPaymentService.initiateBookingPayment(request,myGroupIds)
        ));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<PaymentCheckoutResponse>> checkout(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @Valid @RequestBody CheckoutRequest request) {

        Integer customerId = principal.getCustomerId();

        // Service sẽ check các BookingDetail, tạo Payment, đổi status sang PENDING_PAYMENT
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo yêu cầu thanh toán thành công",
                customerPaymentService.checkoutBookingDetails(customerId, request)
        ));
    }


}