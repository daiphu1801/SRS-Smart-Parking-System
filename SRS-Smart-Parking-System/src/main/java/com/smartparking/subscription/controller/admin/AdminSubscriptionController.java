package com.smartparking.subscription.controller.admin;

import com.smartparking.shared.exception.BusinessException;
import com.smartparking.subscription.dto.response.PackageDetailResponse; // Nhớ tạo file DTO này nhé
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.subscription.entity.*;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.subscription.entity.Package;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.subscription.service.AdminSubscriptionService;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import com.smartparking.shared.exception.BusinessException;
import org.springframework.data.domain.Page;
import com.smartparking.shared.exception.BusinessException;
import org.springframework.data.domain.PageRequest;
import com.smartparking.shared.exception.BusinessException;
import org.springframework.data.domain.Pageable;
import com.smartparking.shared.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import com.smartparking.shared.exception.BusinessException;
import org.springframework.security.access.prepost.PreAuthorize;
import com.smartparking.shared.exception.BusinessException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/subscription")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionService subscriptionService;

    @PreAuthorize("hasAuthority('VEHICLE_TYPE_READ')")
    @GetMapping("/vehicle-types")
    public ResponseEntity<Page<VehicleType>> getVehicleTypes(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscriptionService.getVehicleTypes(keyword, pageable));
    }
    @PreAuthorize("hasAuthority('VEHICLE_TYPE_READ')")
    @GetMapping("/vehicle-types/{id}")
    public ResponseEntity<VehicleType> getVehicleTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getVehicleTypeById(id));
    }
    @PreAuthorize("hasAuthority('VEHICLE_TYPE_CREATE')")
    @PostMapping("/vehicle-types")
    public ResponseEntity<VehicleType> createVehicleType(@RequestBody VehicleType vehicleType) {
        return ResponseEntity.ok(subscriptionService.createVehicleType(vehicleType));
    }
    @PreAuthorize("hasAuthority('VEHICLE_TYPE_UPDATE')")
    @PutMapping("/vehicle-types/{id}")
    public ResponseEntity<VehicleType> updateVehicleType(@PathVariable Integer id, @RequestBody VehicleType updates) {
        return ResponseEntity.ok(subscriptionService.updateVehicleType(id, updates));
    }
    @PreAuthorize("hasAuthority('VEHICLE_TYPE_DELETE')")
    @DeleteMapping("/vehicle-types/{id}")
    public ResponseEntity<Void> deleteVehicleType(@PathVariable Integer id) {
        subscriptionService.deleteVehicleType(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @GetMapping("/packages")
    public ResponseEntity<Page<Package>> getPackages(
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer groupProfileId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscriptionService.getPackages(searchName, status,groupProfileId, pageable));
    }
    @PreAuthorize("hasAuthority('PACKAGE_CREATE')")
    @PostMapping("/packages")
    public ResponseEntity<Package> createPackage(@RequestBody Package pkg) {
        return ResponseEntity.ok(subscriptionService.createPackage(pkg));
    }
    @PreAuthorize("hasAuthority('PACKAGE_UPDATE')")
    @PutMapping("/packages/{id}")
    public ResponseEntity<Package> updatePackage(@PathVariable Integer id, @RequestBody Package updates) {
        return ResponseEntity.ok(subscriptionService.updatePackage(id, updates));
    }

    @PreAuthorize("hasAuthority('PACKAGE_DELETE')")
    @DeleteMapping("/packages/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Integer id) {
        subscriptionService.deletePackage(id);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // 3. API CHI TIẾT GÓI CƯỚC (Dùng để in ra Sơ đồ cây cho FE)
    // ==========================================
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @GetMapping("/packages/{id}/details")
    public ResponseEntity<PackageDetailResponse> getPackageDetails(@PathVariable Integer id) {
        // Hàm này sẽ gom Gói + Loại Xe + Bảng Giá vào 1 cục JSON
        return ResponseEntity.ok(subscriptionService.getPackageDetails(id));
    }

    // ==========================================
    // 4. API MUTATION CHO CẤU HÌNH LOẠI XE VÀ BẢNG GIÁ
    // (FE vẫn cần gọi mấy cái này khi Admin bấm Thêm/Sửa/Xóa 1 dòng con)
    // ==========================================

    // --- Xử lý Gói - Loại Xe ---
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @GetMapping("/package-vehicle-types/{id}")
    public ResponseEntity<PackageVehicleType> getPackageVehicleTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getPackageVehicleTypeById(id));
    }
    @PreAuthorize("hasAuthority('PACKAGE_CREATE')")
    @PostMapping("/package-vehicle-types")
    public ResponseEntity<PackageVehicleType> createPackageVehicleType(@RequestBody PackageVehicleType entity) {
        return ResponseEntity.ok(subscriptionService.createPackageVehicleType(entity));
    }
    @PreAuthorize("hasAuthority('PACKAGE_UPDATE')")
    @PutMapping("/package-vehicle-types/{id}")
    public ResponseEntity<PackageVehicleType> updatePackageVehicleType(@PathVariable Integer id, @RequestBody PackageVehicleType updates) {
        return ResponseEntity.ok(subscriptionService.updatePackageVehicleType(id, updates));
    }
    @PreAuthorize("hasAuthority('PACKAGE_DELETE')")
    @DeleteMapping("/package-vehicle-types/{id}")
    public ResponseEntity<Void> deletePackageVehicleType(@PathVariable Integer id) {
        try{

        subscriptionService.deletePackageVehicleType(id);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa vì có bản ghi đang  sử dụng" + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // --- Xử lý Bảng Giá ---
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @GetMapping("/prices/{id}")
    public ResponseEntity<PackagePrice> getPackagePriceById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getPackagePriceById(id));
    }
    @PreAuthorize("hasAuthority('PACKAGE_CREATE')")
    @PostMapping("/prices")
    public ResponseEntity<PackagePrice> createPackagePrice(@RequestBody PackagePrice entity) {
        return ResponseEntity.ok(subscriptionService.createPackagePrice(entity));
    }
    @PreAuthorize("hasAuthority('PACKAGE_UPDATE')")
    @PutMapping("/prices/{id}")
    public ResponseEntity<PackagePrice> updatePackagePrice(@PathVariable Integer id, @RequestBody PackagePrice updates) {
        return ResponseEntity.ok(subscriptionService.updatePackagePrice(id, updates));
    }
    @PreAuthorize("hasAuthority('PACKAGE_DELETE')")
    @DeleteMapping("/prices/{id}")
    public ResponseEntity<Void> deletePackagePrice(@PathVariable Integer id) {
        try{

        subscriptionService.deletePackagePrice(id);
        } catch (Exception e) {
            PackagePrice p = subscriptionService.getPackagePriceById(id);
            p.setIsActive(false)
            ;
        return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok().build();
    }
}