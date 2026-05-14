package com.smartparking.subscription.controller.customer;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.subscription.dto.BookingMetadataDto;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.subscription.service.CustomerMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/subscription/metadata")
@RequiredArgsConstructor
public class CustomerMetadataController {

    // Service này sau này ông sẽ viết logic để dùng chung cái hàm check Quota
    private final CustomerMetadataService metadataService;

    /**
     * API 1: Khi khách hàng mở Form "Thêm xe mới".
     * Trả về danh sách các Loại xe (Vehicle Type) mà gia đình này CÒN QUYỀN thêm.
     */
    @GetMapping("/allowed-vehicle-types")
    public ResponseEntity<ApiResponse<List<BookingMetadataDto.AllowedVehicleTypeResponse>>> getAllowedVehicleTypes(
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer groupId = principal.getMemberGroupIds().getFirst();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách Loại xe khả dụng thành công",
                metadataService.getAllowedVehicleTypes(groupId)
        ));
    }


    @GetMapping("/available-packages")
    public ResponseEntity<ApiResponse<List<BookingMetadataDto.AvailablePackagePriceResponse>>> getAvailablePackages(
            @AuthenticationPrincipal CustomAccountPrincipal principal,
            @RequestParam("vehicleTypeId") Integer vehicleTypeId) {

        Integer groupId = principal.getMemberGroupIds().getFirst();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách Gói cước khả dụng thành công",
                metadataService.getAvailablePackages(groupId, vehicleTypeId)
        ));
    }
}