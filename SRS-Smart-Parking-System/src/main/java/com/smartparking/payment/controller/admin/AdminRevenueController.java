package com.smartparking.payment.controller.admin;

import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.PaymentReconciliationResponse;
import com.smartparking.payment.dto.response.dashboard.PaymentKpiResponse;
import com.smartparking.payment.dto.response.dashboard.PaymentMethodShareResponse;
import com.smartparking.payment.dto.response.dashboard.RevenueTimeSeriesResponse;
import com.smartparking.payment.dto.response.dashboard.TopCustomerRevenueResponse;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.service.AdminPaymentService;
import com.smartparking.payment.service.DashboardPaymentService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/revenues")
@RequiredArgsConstructor
public class AdminRevenueController {
        private final DashboardPaymentService dashboardService;

        // 1. Thẻ KPI Tổng quan
        @PreAuthorize("hasAuthority('REPORT_READ')")
        @PostMapping("/kpis")
        public ResponseEntity<ApiResponse<PaymentKpiResponse>> getPaymentKpis(
                @RequestBody PaymentFilterRequest filter) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Lấy KPI thành công",
                        dashboardService.getPaymentKpis(filter)
                ));
        }

        // 2. Biểu đồ Đường/Cột: Doanh thu theo thời gian
        @PreAuthorize("hasAuthority('REPORT_READ')")
        @PostMapping("/revenue-time-series")
        public ResponseEntity<ApiResponse<List<RevenueTimeSeriesResponse>>> getRevenueTimeSeries(
                @RequestBody PaymentFilterRequest filter) {
                // Ghi chú: Service sẽ tự bóc ngày từ filter để GROUP BY ngày hoặc giờ
                return ResponseEntity.ok(ApiResponse.success(
                        "Lấy dữ liệu chuỗi thời gian thành công",
                        dashboardService.getRevenueTimeSeries(filter)
                ));
        }

        // 3. Biểu đồ Tròn: Tỷ trọng cổng thanh toán
        @PreAuthorize("hasAuthority('REPORT_READ')")
        @PostMapping("/method-shares")
        public ResponseEntity<ApiResponse<List<PaymentMethodShareResponse>>> getMethodShares(
                @RequestBody PaymentFilterRequest filter) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Lấy dữ liệu tỷ trọng thành công",
                        dashboardService.getMethodShares(filter)
                ));
        }

        // 4. Bảng xếp hạng: Top Khách Hàng VIP
        @PreAuthorize("hasAuthority('REPORT_READ')")
        @PostMapping("/top-customers")
        public ResponseEntity<ApiResponse<List<TopCustomerRevenueResponse>>> getTopCustomers(
                @RequestBody PaymentFilterRequest filter,
                @RequestParam(defaultValue = "10") int limit) { // Cho phép truyền limit, mặc định top 10
                return ResponseEntity.ok(ApiResponse.success(
                        "Lấy Top khách hàng thành công",
                        dashboardService.getTopCustomers(filter, limit)
                ));
        }
}