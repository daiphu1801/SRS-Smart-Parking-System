package com.smartparking.subscription.repository;

import com.smartparking.subscription.entity.PackagePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackagePriceRepository extends JpaRepository<PackagePrice, Integer> {
    List<PackagePrice> findByPkgVehTypeId(Integer pkgVehTypeId);

    List<PackagePrice> findByPkgVehTypeIdAndIsActiveTrue(Integer pkgVehTypeId);
}
