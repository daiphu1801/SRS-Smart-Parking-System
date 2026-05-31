package com.smartparking.operation.repository;

import com.smartparking.operation.entity.ParkingSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long>, JpaSpecificationExecutor<ParkingSession> {

    @Override
    @EntityGraph(attributePaths = {"bookingDetail", "vehicleType", "zoneIn", "zoneOut"})
    Page<ParkingSession> findAll(@Nullable Specification<ParkingSession> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"bookingDetail", "vehicleType", "zoneIn", "zoneOut"})
    Optional<ParkingSession> findById(Long id);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.vehicleNo = :plate AND ps.exitTime IS NULL")
    Optional<ParkingSession> findOpenSession(@Param("plate") String plate);

    @Query("SELECT ps FROM ParkingSession ps " +
            "WHERE ps.bookingDetailId IN " +
            "  (SELECT bd.id FROM BookingDetail bd WHERE bd.customerId = :customerId) " +
            "ORDER BY ps.entryTime DESC")
    List<ParkingSession> findByCustomerId(@Param("customerId") Integer customerId);

//    List<ParkingSession> findByVehicleNoOrderByEntryTimeDesc(String vehicleNo);

    boolean existsByVehicleNoAndEntryTime(String vehicleNo, LocalDateTime entryTime);

    Optional<ParkingSession> findFirstByVehicleNoAndExitTimeIsNullOrderByIdDesc(String vehicleNo);
    List<ParkingSession> findByVehicleNoInAndExitTimeIsNull(List<String> vehicleNos);

    @Query("SELECT ps FROM ParkingSession ps " +
            "WHERE ps.exitTime IS NULL " +
            "AND ps.bookingDetailId IN " +
            "  (SELECT bd.id FROM BookingDetail bd WHERE bd.customerId = :customerId) " +
            "ORDER BY ps.entryTime DESC")
    List<ParkingSession> findOpenSessionsByCustomerId(@Param("customerId") Integer customerId);

}
