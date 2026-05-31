package com.smartparking.operation.controller.customer;

import com.smartparking.operation.dto.response.CustomerHomeDashboardResponse;
import com.smartparking.operation.service.customer.CustomerHomeDashboardService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.CustomAccountPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint tổng hợp dữ liệu Home Dashboard cho Customer App.
 *
 * GET /api/v1/customer/home
 *
 * Trả về 1 DTO duy nhất (CustomerHomeDashboardResponse) thay vì bắt
 * Mobile phải gọi 4-5 API riêng lẻ rồi tự ghép – giảm network round-trip,
 * tăng performance và đảm bảo security scope tại Backend.
 */
@RestController
@RequestMapping("/api/v1/customer/home")
@RequiredArgsConstructor
public class CustomerHomeDashboardController {

    private final CustomerHomeDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<CustomerHomeDashboardResponse>> getHomeDashboard(
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer customerId = principal.getCustomerId();
        if (customerId == null) {
            throw new RuntimeException("Tài khoản của bạn không được gắn với hồ sơ Khách hàng nào!");
        }

        CustomerHomeDashboardResponse dashboard = dashboardService.buildDashboard(customerId);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin trang chủ thành công",
                dashboard
        ));
    }
}
