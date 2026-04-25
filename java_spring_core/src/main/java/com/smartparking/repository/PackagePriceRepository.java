package com.smartparking.repository;

import com.smartparking.entity.PackagePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackagePriceRepository extends JpaRepository<PackagePrice, Integer> {
    List<PackagePrice> findByIsActiveTrue();
}
