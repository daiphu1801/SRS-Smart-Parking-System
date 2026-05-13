package com.smartparking.subscription.controller.admin;

import com.smartparking.subscription.dto.response.PackageDetailResponse; // Nhớ tạo file DTO này nhé
import com.smartparking.subscription.entity.*;
import com.smartparking.subscription.entity.Package;
import com.smartparking.subscription.service.AdminSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/subscription")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionService subscriptionService;


    @GetMapping("/vehicle-types")
    public ResponseEntity<Page<VehicleType>> getVehicleTypes(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscriptionService.getVehicleTypes(keyword, pageable));
    }

    @GetMapping("/vehicle-types/{id}")
    public ResponseEntity<VehicleType> getVehicleTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getVehicleTypeById(id));
    }

    @PostMapping("/vehicle-types")
    public ResponseEntity<VehicleType> createVehicleType(@RequestBody VehicleType vehicleType) {
        return ResponseEntity.ok(subscriptionService.createVehicleType(vehicleType));
    }

    @PutMapping("/vehicle-types/{id}")
    public ResponseEntity<VehicleType> updateVehicleType(@PathVariable Integer id, @RequestBody VehicleType updates) {
        return ResponseEntity.ok(subscriptionService.updateVehicleType(id, updates));
    }

    @DeleteMapping("/vehicle-types/{id}")
    public ResponseEntity<String> deleteVehicleType(@PathVariable Integer id) {
        subscriptionService.deleteVehicleType(id);
        return ResponseEntity.ok("Xóa loại phương tiện thành công");
    }


    @GetMapping("/packages")
    public ResponseEntity<Page<Package>> getPackages(
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscriptionService.getPackages(searchName, status, pageable));
    }

    @PostMapping("/packages")
    public ResponseEntity<Package> createPackage(@RequestBody Package pkg) {
        return ResponseEntity.ok(subscriptionService.createPackage(pkg));
    }

    @PutMapping("/packages/{id}")
    public ResponseEntity<Package> updatePackage(@PathVariable Integer id, @RequestBody Package updates) {
        return ResponseEntity.ok(subscriptionService.updatePackage(id, updates));
    }

    @DeleteMapping("/packages/{id}")
    public ResponseEntity<String> deletePackage(@PathVariable Integer id) {
        subscriptionService.deletePackage(id);
        return ResponseEntity.ok("Xóa gói cước thành công");
    }

    // ==========================================
    // 3. API CHI TIẾT GÓI CƯỚC (Dùng để in ra Sơ đồ cây cho FE)
    // ==========================================

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

    @GetMapping("/package-vehicle-types/{id}")
    public ResponseEntity<PackageVehicleType> getPackageVehicleTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getPackageVehicleTypeById(id));
    }

    @PostMapping("/package-vehicle-types")
    public ResponseEntity<PackageVehicleType> createPackageVehicleType(@RequestBody PackageVehicleType entity) {
        return ResponseEntity.ok(subscriptionService.createPackageVehicleType(entity));
    }

    @PutMapping("/package-vehicle-types/{id}")
    public ResponseEntity<PackageVehicleType> updatePackageVehicleType(@PathVariable Integer id, @RequestBody PackageVehicleType updates) {
        return ResponseEntity.ok(subscriptionService.updatePackageVehicleType(id, updates));
    }

    @DeleteMapping("/package-vehicle-types/{id}")
    public ResponseEntity<String> deletePackageVehicleType(@PathVariable Integer id) {
        subscriptionService.deletePackageVehicleType(id);
        return ResponseEntity.ok("Xóa cấu hình thành công");
    }

    // --- Xử lý Bảng Giá ---
    @GetMapping("/prices/{id}")
    public ResponseEntity<PackagePrice> getPackagePriceById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.getPackagePriceById(id));
    }

    @PostMapping("/prices")
    public ResponseEntity<PackagePrice> createPackagePrice(@RequestBody PackagePrice entity) {
        return ResponseEntity.ok(subscriptionService.createPackagePrice(entity));
    }

    @PutMapping("/prices/{id}")
    public ResponseEntity<PackagePrice> updatePackagePrice(@PathVariable Integer id, @RequestBody PackagePrice updates) {
        return ResponseEntity.ok(subscriptionService.updatePackagePrice(id, updates));
    }

    @DeleteMapping("/prices/{id}")
    public ResponseEntity<String> deletePackagePrice(@PathVariable Integer id) {
        subscriptionService.deletePackagePrice(id);
        return ResponseEntity.ok("Xóa mức giá thành công");
    }
}