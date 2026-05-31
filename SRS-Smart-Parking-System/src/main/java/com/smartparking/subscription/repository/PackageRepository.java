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

    Optional<Package> findByProfileId(Integer profileId);

    boolean existsByPackageCode(String packageCode);

    @Query("SELECT p FROM Package p " +
            "LEFT JOIN GroupsProfile gp ON p.profileId = gp.id " +
            "WHERE (:status IS NULL OR p.isAvailable = :status) " +
            "AND (:profileId IS NULL OR p.profileId = :profileId) " +
            "AND (CAST(:search AS String) = '%' OR " +
            "LOWER(p.packageCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.packageName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(gp.profileCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(gp.profileName) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ")")
    Page<Package> filterDynamic(
            @Param("search") String search,
            @Param("status") Boolean status,
            @Param("profileId") Integer profileId,
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
