package com.smartparking.operation.repository;

import com.smartparking.operation.dto.response.BookingResponse;
import com.smartparking.operation.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Dùng JPQL Constructor để đắp data thẳng vào DTO
    @Query("SELECT new com.smartparking.operation.dto.response.BookingResponse(" +
            "b.id, b.createdAt, " +
            "g.id, g.groupName, g.groupCode, " +
            "p.id, p.packagePriceName, " +
            "a.id, a.username) " +
            "FROM Booking b " +
            "LEFT JOIN GroupsCustomer g ON b.groupId = g.id " +
            "LEFT JOIN PackagePrice p ON b.packageId = p.id " +
            "LEFT JOIN Account a ON b.createdBy = a.id " +
            "WHERE (:groupId IS NULL OR b.groupId = :groupId) " +
            "AND (:packageId IS NULL OR b.packageId = :packageId)")
    Page<BookingResponse> findAllBookingsWithDetails(
            @Param("groupId") Integer groupId,
            @Param("packageId") Integer packageId,
            Pageable pageable);

    @Query("SELECT new com.smartparking.operation.dto.response.BookingResponse(" +
            "b.id, b.createdAt, " +
            "g.id, g.groupName, g.groupCode, " +
            "p.id, p.packagePriceName, " +
            "a.id, a.username) " +
            "FROM Booking b " +
            "LEFT JOIN GroupsCustomer g ON b.groupId = g.id " +
            "LEFT JOIN PackagePrice p ON b.packageId = p.id " +
            "LEFT JOIN Account a ON b.createdBy = a.id " +
            "WHERE b.id = :id")
    Optional<BookingResponse> findBookingDetailById(@Param("id") Integer id);

    Optional<Booking> findByGroupId(Integer groupId);


}
