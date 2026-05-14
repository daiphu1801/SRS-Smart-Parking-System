package com.smartparking.payment.controller.admin;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.dto.request.TariffRuleCreateRequest;
import com.smartparking.payment.dto.request.TariffRuleFilterRequest;
import com.smartparking.payment.dto.response.TariffRuleResponse;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.service.TariffRuleService;
import com.smartparking.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tariff-rules")
@RequiredArgsConstructor
public class AdminTariffRuleController {

    private final TariffRuleService tariffRuleService;

    // 1. LẤY DANH SÁCH (Hỗ trợ Lọc & Phân trang)
    @PreAuthorize("hasAuthority('TARIFF_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TariffRuleResponse>>> getTariffRules(
            @RequestBody TariffRuleFilterRequest filter,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách bảng giá thành công",
                tariffRuleService.getTariffRules(filter.getVehicleTypeId(),filter.getDayType(), filter.getIsActive(), pageable)
        ));
    }

    // 2. LẤY CHI TIẾT 1 BẢNG GIÁ
    @PreAuthorize("hasAuthority('TARIFF_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TariffRuleResponse>> getTariffRuleDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết bảng giá thành công",
                tariffRuleService.getTariffRuleById(id)
        ));
    }

    // 3. TẠO MỚI BẢNG GIÁ
    @PreAuthorize("hasAuthority('TARIFF_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<TariffRuleResponse>> createTariffRule(
            @Valid @RequestBody TariffRuleCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo bảng giá mới thành công",
                tariffRuleService.createTariffRule(request)
        ));
    }

    // 4. CẬP NHẬT BẢNG GIÁ
    @PreAuthorize("hasAuthority('TARIFF_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TariffRuleResponse>> updateTariffRule(
            @PathVariable Integer id,
            @Valid @RequestBody TariffRuleCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật bảng giá thành công",
                tariffRuleService.updateTariffRule(id, request)
        ));
    }
    @PreAuthorize("hasAuthority('TARIFF_UPDATE')")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableTariffRule(@PathVariable Integer id) {
        tariffRuleService.disableTariffRule(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Đã vô hiệu hóa bảng giá thành công",
                null
        ));
    }
}