package com.smartparking.subscription.repository;

import com.smartparking.subscription.entity.PackageVehicleType;
import com.smartparking.subscription.entity.PackageVehicleType.PackageVehicleTypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageVehicleTypeRepository extends JpaRepository<PackageVehicleType, PackageVehicleTypeId> {
}
