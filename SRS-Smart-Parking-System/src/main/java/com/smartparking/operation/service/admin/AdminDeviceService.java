package com.smartparking.operation.service.admin;

import com.smartparking.operation.entity.DeviceStatus;
import com.smartparking.operation.entity.IoTDevice;
import com.smartparking.operation.repository.IoTDeviceRepository;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminDeviceService {

    private final IoTDeviceRepository ioTDeviceRepository;
    private final ZoneRepository zoneRepository;


    public List<IoTDevice> getDevicesByZone(Integer id) {

        return ioTDeviceRepository.findDevicesByZoneId(id);
    }
    public List<IoTDevice> getAllDevices() {

        return ioTDeviceRepository.findAll();
    }

    public IoTDevice getDeviceById(Integer id) {
        return ioTDeviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Lỗi: Không tìm thấy thiết bị với ID: " + id));
    }

    @Transactional
    public IoTDevice createDevice(IoTDevice device) {
        // 1. Validate: Mã thiết bị không được trùng
        if (ioTDeviceRepository.existsByDeviceCode(device.getDeviceCode())) {
            throw new BusinessException("Lỗi: Mã thiết bị (Device Code) này đã tồn tại trên hệ thống!");
        }

        validateZonesExist(device.getZoneIdFrom(), device.getZoneIdTo());

        if (device.getStatus() == null) {
            device.setStatus(DeviceStatus.OFFLINE);
        }

        return ioTDeviceRepository.save(device);
    }

    @Transactional
    public IoTDevice updateDevice(Integer id, IoTDevice updates) {
        IoTDevice existing = getDeviceById(id);

        if (updates.getDeviceCode() != null && !existing.getDeviceCode().equals(updates.getDeviceCode())) {
            if (ioTDeviceRepository.existsByDeviceCode(updates.getDeviceCode())) {
                throw new BusinessException("Lỗi: Mã thiết bị này đã được sử dụng cho một máy khác!");
            }
            existing.setDeviceCode(updates.getDeviceCode());
        }

        validateZonesExist(updates.getZoneIdFrom(), updates.getZoneIdTo());
        existing.setZoneIdFrom(updates.getZoneIdFrom());
        existing.setZoneIdTo(updates.getZoneIdTo());

        existing.setDeviceName(updates.getDeviceName());
        existing.setIpAddress(updates.getIpAddress());
        existing.setDeviceType(updates.getDeviceType());
        existing.setDirection(updates.getDirection());

        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }

        return ioTDeviceRepository.save(existing);
    }

    @Transactional
    public void deleteDevice(Integer id) {
        IoTDevice existing = getDeviceById(id);

        ioTDeviceRepository.delete(existing);
    }


    private void validateZonesExist(Integer zoneIdFrom, Integer zoneIdTo) {
        if (zoneIdFrom != null && !zoneRepository.existsById(zoneIdFrom)) {
            throw new BusinessException("Lỗi: Khu vực xuất phát (Zone From) không tồn tại!");
        }
        if (zoneIdTo != null && !zoneRepository.existsById(zoneIdTo)) {
            throw new BusinessException("Lỗi: Khu vực đích đến (Zone To) không tồn tại!");
        }
    }
}
