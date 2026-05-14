package com.smartparking.operation.repository;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {

       @Query("SELECT bd FROM BookingDetail bd " +
                     "WHERE bd.vehicleNo = :plate AND bd.status = 'ACTIVE' AND bd.endDate > :now")
       Optional<BookingDetail> findActiveByVehicleNo(@Param("plate") String plate,
                     @Param("now") LocalDateTime now);

       @Query("SELECT bd FROM BookingDetail bd " +
                     "WHERE bd.status = 'ACTIVE' AND bd.endDate BETWEEN :from AND :to")
       List<BookingDetail> findExpiringBetween(@Param("from") LocalDateTime from,
                     @Param("to") LocalDateTime to);

       List<BookingDetail> findByCustomerIdAndStatus(Integer customerId, BookingStatus status);

       List<BookingDetail> findByCustomerId(Integer customerId);
}
