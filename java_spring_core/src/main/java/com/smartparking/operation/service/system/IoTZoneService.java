package com.smartparking.operation.service.system;

import com.smartparking.operation.dto.response.ZoneTreeResponse;
import com.smartparking.operation.entity.IoTDevice;
import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.IoTDeviceRepository;
import com.smartparking.operation.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class IoTZoneService {

    private final ZoneRepository zoneRepository;
    private final IoTDeviceRepository iotDevceRepository;


    @Transactional
    public void updateZoneTransition(Integer deviceId) {
        // 1. Dùng findById và xử lý lỗi thanh lịch, không dùng getReferenceById
        IoTDevice device = iotDevceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị IoT ID: " + deviceId));

        Integer zoneFromId = device.getZoneIdFrom();
        Integer zoneToId = device.getZoneIdTo();

        // 2. Chuyển giao nhiệm vụ cộng trừ thẳng xuống DB để chống Race Condition
        if (zoneFromId != null) {
            zoneRepository.updateOccupancy(zoneFromId, -1);
        }
        if (zoneToId != null) {
            zoneRepository.updateOccupancy(zoneToId, 1);
        }

        // Xong! Không cần get data lên, không cần Dirty Checking, không cần save() hay tạo List rườm rà.
    }




}
