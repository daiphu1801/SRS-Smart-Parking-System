package com.smartparking.operation.repository;

import com.smartparking.operation.entity.IoTDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IoTDeviceRepository extends JpaRepository<IoTDevice, Integer> {
    @Query("SELECT d FROM IoTDevice d WHERE d.zoneIdFrom = :zoneId OR d.zoneIdTo = :zoneId")
    List<IoTDevice> findByZoneId(@Param("zoneId") Integer zoneId);
}
