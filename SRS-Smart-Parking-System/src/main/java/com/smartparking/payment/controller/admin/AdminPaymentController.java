package com.smartparking.payment.controller.admin;

import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.PaymentReconciliationResponse;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.service.AdminPaymentService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

        private final AdminPaymentService adminPaymentService;

        @PreAuthorize("hasAuthority('PAYMENT_READ')")
        @PostMapping()
        public ResponseEntity<ApiResponse<Page<PaymentResponse>>> searchPayments(
                        @RequestBody PaymentFilterRequest filter,
                        Pageable pageable) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy danh sách thanh toán thành công",
                                adminPaymentService.searchPayments(filter, pageable)));
        }

        @PreAuthorize("hasAuthority('PAYMENT_READ')")
        @GetMapping("/reconciliation-exceptions")
        public ResponseEntity<ApiResponse<Page<PaymentReconciliationResponse>>> getReconciliationExceptions(
                Pageable pageable) {

                return ResponseEntity.ok(ApiResponse.success(
                        "Lấy danh sách ngoại lệ đối soát hệ thống thành công",
                        adminPaymentService.getReconciliationExceptions(pageable)));
        }

        // 2. GET CHI TIẾT 1 GIAO DỊCH CƠ BẢN (Không kèm details)
        @PreAuthorize("hasAuthority('PAYMENT_READ')")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy chi tiết giao dịch thành công",
                                adminPaymentService.getPaymentById(id)));
        }

        // 3. GET DẠNG CÂY (Payment Cha + List PaymentDetail Con)
        @PreAuthorize("hasAuthority('PAYMENT_READ')")
        @GetMapping("/{id}/details")
        public ResponseEntity<ApiResponse<PaymentTreeResponse>> getPaymentTreeDetails(
                        @PathVariable Long id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy chi tiết giao dịch (dạng cây) thành công",
                                adminPaymentService.getPaymentTreeDetails(id)));
        }

        @PreAuthorize("hasAuthority('PAYMENT_RESOLVE')")
        @PostMapping("/{id}/resolve")
        public ResponseEntity<ApiResponse<Void>> resolvePayment(
                        @PathVariable Long id) {

                adminPaymentService.resolvePayment(id);

                return ResponseEntity.ok(ApiResponse.success(
                                "Đã xử lý và kích hoạt giao dịch thành công",
                                null));
        }

        @PreAuthorize("hasAuthority('PAYMENT_RESOLVE')")
        @PostMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<Void>> cancelPayment(
                @PathVariable Long id) {

                adminPaymentService.cancelPayment(id);
                return ResponseEntity.ok(ApiResponse.success(
                        "Đã hủy giao dịch giao dịch thành công",
                        null));
        }
}