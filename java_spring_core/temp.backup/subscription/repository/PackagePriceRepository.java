package com.smartparking.subscription.repository;

import com.smartparking.subscription.entity.PackagePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackagePriceRepository extends JpaRepository<PackagePrice, Integer> {
}
