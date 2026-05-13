package com.smartparking.operation.repository;


import com.smartparking.operation.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    List<Zone> findByParentZoneId(Integer parentZoneId);

    List<Zone> findByParentZoneIdIsNull();

    boolean existsByParentZoneId(Integer parentZoneId);

    @Modifying
    @Query("UPDATE Zone z SET z.currentOccupancy = z.currentOccupancy + :value WHERE z.id = :zoneId")
    void updateOccupancy(@Param("zoneId") Integer zoneId, @Param("value") int value);
}
