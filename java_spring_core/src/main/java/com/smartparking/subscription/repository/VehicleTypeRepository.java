package com.smartparking.subscription.repository;

import com.smartparking.subscription.entity.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Integer> {
    // Omni-search: Lọc theo cả Code hoặc Name
    @Query("SELECT v FROM VehicleType v WHERE " +
            "LOWER(v.typeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.typeName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<VehicleType> searchByCodeOrName(@Param("search") String search, Pageable pageable);

    // Dùng để validate không cho tạo trùng Code
    boolean existsByTypeCode(String typeCode);
}
