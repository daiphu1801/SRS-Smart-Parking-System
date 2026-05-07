package com.smartparking.operation.repository;

import com.smartparking.entity.IoTDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IoTDeviceRepository extends JpaRepository<IoTDevice, Integer> {
    List<IoTDevice> findByZoneId(Integer zoneId);
}
