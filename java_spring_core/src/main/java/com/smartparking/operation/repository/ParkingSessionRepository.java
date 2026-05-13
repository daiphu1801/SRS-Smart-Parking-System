package com.smartparking.operation.repository;

import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.vehicleNo = :plate AND ps.exitTime IS NULL")
    Optional<ParkingSession> findOpenSession(@Param("plate") String plate);

    @Query("SELECT ps FROM ParkingSession ps " +
            "WHERE ps.bookingDetailId IN " +
            "  (SELECT bd.id FROM BookingDetail bd WHERE bd.customerId = :customerId) " +
            "ORDER BY ps.entryTime DESC")
    List<ParkingSession> findByCustomerId(@Param("customerId") Integer customerId);

    List<ParkingSession> findByVehicleNoOrderByEntryTimeDesc(String vehicleNo);

    List<ParkingSession> findByVehicleNoInOrderByEntryTimeDesc(Collection<String> vehicleNos);
}
