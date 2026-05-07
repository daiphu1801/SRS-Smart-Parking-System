package com.smartparking.subscription.controller.admin;

import com.smartparking.subscription.entity.*;
import com.smartparking.subscription.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminPricingController {

    private final VehicleTypeRepository vehicleTypeRepo;
    private final PackageRepository packageRepo;
    private final PackagePriceRepository packagePriceRepo;

    // --- 2.3 Bảng giá & Hạn mức ---
    @GetMapping("/vehicle-types")
    public ResponseEntity<List<VehicleType>> listVehicleTypes() {
        return ResponseEntity.ok(vehicleTypeRepo.findAll());
    }

    @GetMapping("/packages")
    public ResponseEntity<List<Package>> listPackages() {
        return ResponseEntity.ok(packageRepo.findAll());
    }

    @PostMapping("/packages")
    public ResponseEntity<Package> createPackage(@RequestBody Package pkg) {
        return ResponseEntity.ok(packageRepo.save(pkg));
    }

    @GetMapping("/package-prices")
    public ResponseEntity<List<PackagePrice>> listPackagePrices() {
        return ResponseEntity.ok(packagePriceRepo.findAll());
    }

    // Note: tariff-rules requires TariffRuleRepository (omitted here for brevity, assume exists or will be added)
}
