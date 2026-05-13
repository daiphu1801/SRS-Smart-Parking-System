package com.smartparking.payment.controller.admin;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.service.AdminPaymentService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

        private final AdminPaymentService adminPaymentService;

        // 1. GET ALL CÓ FILTER ĐỘNG (Dùng POST để gửi cục Filter to)
        @PostMapping()
        public ResponseEntity<ApiResponse<Page<PaymentResponse>>> searchPayments(
                        @RequestBody PaymentFilterRequest filter,
                        Pageable pageable) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy danh sách thanh toán thành công",
                                adminPaymentService.searchPayments(filter, pageable)));
        }

        // 2. GET CHI TIẾT 1 GIAO DỊCH CƠ BẢN (Không kèm details)
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy chi tiết giao dịch thành công",
                                adminPaymentService.getPaymentById(id)));
        }

        // 3. GET DẠNG CÂY (Payment Cha + List PaymentDetail Con)
        @GetMapping("/{id}/details")
        public ResponseEntity<ApiResponse<PaymentTreeResponse>> getPaymentTreeDetails(
                        @PathVariable Long id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy chi tiết giao dịch (dạng cây) thành công",
                                adminPaymentService.getPaymentTreeDetails(id)));
        }

        @PostMapping("/{id}/resolve")
        public ResponseEntity<ApiResponse<Void>> resolvePayment(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomAccountPrincipal principal) {

                // Truyền ID đơn và ID của Admin (để lưu vết Audit xem ai là người duyệt)
                adminPaymentService.resolvePayment(id, principal.getEmployeeId());

                return ResponseEntity.ok(ApiResponse.success(
                                "Đã xử lý và kích hoạt giao dịch thành công",
                                null));
        }
}