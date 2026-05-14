package com.smartparking.operation.repository;

import com.smartparking.operation.entity.IoTDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IoTDeviceRepository extends JpaRepository<IoTDevice, Integer> {

    // Lấy thiết bị 2 chiều (Vào hoặc Ra đều thuộc Zone đó)
    @Query("SELECT d FROM IoTDevice d WHERE d.zoneIdFrom = :zoneId OR d.zoneIdTo = :zoneId")
    List<IoTDevice> findDevicesByZoneId(@Param("zoneId") Integer zoneId);


    @Query("SELECT COUNT(d) > 0 FROM IoTDevice d WHERE d.zoneIdFrom = :zoneId OR d.zoneIdTo = :zoneId")
    boolean existsByZoneId(@Param("zoneId") Integer zoneId);

    boolean existsByDeviceCode(String deviceCode);
}
