package com.smartparking.controller;

import com.smartparking.repository.PackagePriceRepository;
import com.smartparking.repository.PackageRepository;
import com.smartparking.entity.Package;
import com.smartparking.entity.PackagePrice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PricingController {

    private final PackageRepository packageRepo;
    private final PackagePriceRepository packagePriceRepo;

    @GetMapping("/api/packages")
    public ResponseEntity<List<Package>> listPackages() {
        return ResponseEntity.ok(packageRepo.findAll());
    }

    @GetMapping("/api/packages/{id}/prices")
    public ResponseEntity<List<PackagePrice>> listPrices(@PathVariable Integer id) {
        return ResponseEntity.ok(packagePriceRepo.findByIsActiveTrue());
    }

    @PostMapping("/api/admin/packages")
    public ResponseEntity<Package> createPackage(@RequestBody Package pkg) {
        return ResponseEntity.ok(packageRepo.save(pkg));
    }

    @PutMapping("/api/admin/packages/{id}")
    public ResponseEntity<Package> updatePackage(@PathVariable Integer id, @RequestBody Package pkg) {
        pkg.setId(id);
        return ResponseEntity.ok(packageRepo.save(pkg));
    }

    @DeleteMapping("/api/admin/packages/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Integer id) {
        packageRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
