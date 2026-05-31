package com.smartparking.subscription.repository;

import com.smartparking.subscription.dto.response.PackageVehicleTypeResponse;
import com.smartparking.subscription.entity.PackageVehicleType;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageVehicleTypeRepository extends JpaRepository<PackageVehicleType, Integer> {
    boolean existsByPackageIdAndVehicleTypeId(Integer packageId, Integer vehicleTypeId);

    List<PackageVehicleType> findByPackageId(Integer packageId);
    Optional<PackageVehicleType> findByPackageIdAndVehicleTypeId(Integer packageId, Integer vehicleTypeId);

    @Query("SELECT pvt.id as id, " +
            "pvt.packageId as packageId, " +
            "pvt.vehicleTypeId as vehicleTypeId, " +
            "pvt.maxQuantity as maxQuantity, " +
            "vt.typeCode as vehicleTypeCode, " +
            "vt.typeName as vehicleTypeName " +
            "FROM PackageVehicleType pvt " +
            "JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +
            "WHERE pvt.packageId = :packageId")
    List<PackageVehicleTypeResponse> findByPackageIdWithVehicleInfo(@Param("packageId") Integer packageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    @Query("SELECT pvt FROM PackageVehicleType pvt WHERE pvt.id = :id")
    Optional<PackageVehicleType> findByIdWithLock(@Param("id") Integer id);


}
