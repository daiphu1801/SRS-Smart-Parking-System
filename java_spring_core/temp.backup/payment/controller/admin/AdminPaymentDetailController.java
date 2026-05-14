package com.smartparking.payment.controller.admin;

import com.smartparking.payment.dto.request.payment.PaymentDetailFilterRequest;
import com.smartparking.payment.dto.response.payment.PaymentDetailResponse;
import com.smartparking.payment.service.PaymentDetailService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payment-details")
@RequiredArgsConstructor
public class AdminPaymentDetailController {
    private final PaymentDetailService paymentDetailService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentDetailResponse>>> searchPaymentDetails(
            @RequestBody PaymentDetailFilterRequest filterRequest,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách chi tiết thanh toán thành công",
                paymentDetailService.searchPaymentDetails(filterRequest, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentDetailById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết thanh toán thành công",
                paymentDetailService.getPaymentDetailById(id)
        ));
    }
}