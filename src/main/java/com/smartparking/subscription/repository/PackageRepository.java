package com.smartparking.subscription.repository;

import com.smartparking.subscription.dto.response.PackageWithProfileInfo;
import com.smartparking.subscription.entity.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Integer> {
    // 1. Kiểm tra trùng mã Gói
    boolean existsByPackageCode(String packageCode);

    @Query("SELECT p FROM Package p " +
            "LEFT JOIN GroupsProfile gp ON p.profileId = gp.id " +
            "WHERE (:status IS NULL OR p.isAvailable = :status) AND " +
            "(LOWER(p.packageCode) LIKE :search OR " +
            "LOWER(p.packageName) LIKE :search OR " +
            "LOWER(gp.profileCode) LIKE :search OR " +  // Thay đổi tên biến cho đúng với Entity của ông nhé
            "LOWER(gp.profileName) LIKE :search)")
    Page<Package> filterDynamic(
            @Param("search") String search,
            @Param("status") Boolean status,
            Pageable pageable
    );

    @Query("SELECT p.id as packageId, " +
            "p.profileId as profileId, " +
            "gp.profileName as profileName, " +
            "p.packageCode as packageCode, " +
            "p.packageName as packageName, " +
            "p.description as description " +
            "FROM Package p " +
            "LEFT JOIN GroupsProfile gp ON p.profileId = gp.id " +
            "WHERE p.id = :id")
    Optional<PackageWithProfileInfo> findPackageWithProfileById(@Param("id") Integer id);
}
