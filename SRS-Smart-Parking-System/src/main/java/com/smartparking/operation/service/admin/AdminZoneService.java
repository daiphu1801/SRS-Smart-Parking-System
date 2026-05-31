package com.smartparking.operation.service.admin;

import com.smartparking.operation.dto.response.ZoneTreeResponse;
import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.IoTDeviceRepository;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AdminZoneService {

    private final IoTDeviceRepository ioTDeviceRepository;
    private final ZoneRepository zoneRepository;

    public List<ZoneTreeResponse> getZoneTree() {
        List<Zone> allZones = zoneRepository.findAll();

        Map<Integer, ZoneTreeResponse> dtoMap = new HashMap<>();
        List<ZoneTreeResponse> rootNodes = new ArrayList<>();

        for (Zone zone : allZones) {
            ZoneTreeResponse dto = ZoneTreeResponse.builder()
                    .id(zone.getId())
                    .parentZoneId(zone.getParentZoneId())
                    .zoneName(zone.getZoneName())
                    .zoneType(zone.getZoneType())
                    .capacity(zone.getCapacity())
                    .currentOccupancy(zone.getCurrentOccupancy())
                    .children(new ArrayList<>())
                    .build();
            dtoMap.put(dto.getId(), dto);
        }

        // Bước 2: Xếp hình (Nhét con vào bụng cha)
        for (ZoneTreeResponse node : dtoMap.values()) {
            if (node.getParentZoneId() == null) {
                // Nếu không có cha -> Nó là cụ tổ (Root node)
                rootNodes.add(node);
            } else {
                // Nếu có cha -> Móc thằng cha từ Map ra, nhét mình vào list children của nó
                ZoneTreeResponse parent = dtoMap.get(node.getParentZoneId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    // Cứu hộ: Nhỡ parentZoneId bị rác (cha bị xóa mất) thì cho làm Root luôn để khỏi mất data
                    rootNodes.add(node);
                }
            }
        }
        return rootNodes;
    }

    public Zone getZoneById(Integer id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Lỗi: Không tìm thấy Khu vực với ID: " + id));
    }

    @Transactional
    public Zone createZone(Zone zone) {
        // Validate: Nếu có chọn Zone Cha, phải đảm bảo Cha tồn tại
        if (zone.getParentZoneId() != null) {
            if (!zoneRepository.existsById(zone.getParentZoneId())) {
                throw new BusinessException("Lỗi: Khu vực cha không tồn tại!");
            }
        }
        return zoneRepository.save(zone);
    }
    @Transactional
    public List<Zone> getZones() {
        return zoneRepository.findAll();
    }

    @Transactional
    public Zone updateZone(Integer id, Zone updates) {
        Zone existing = getZoneById(id);

        // Chống ngáo: Không cho phép tự chọn bản thân làm Khu vực cha (gây lặp vô hạn)
        if (updates.getParentZoneId() != null && updates.getParentZoneId().equals(id)) {
            throw new BusinessException("Lỗi logic: Một khu vực không thể tự làm cha của chính nó!");
        }

        // Validate Cha tồn tại
        if (updates.getParentZoneId() != null && !zoneRepository.existsById(updates.getParentZoneId())) {
            throw new BusinessException("Lỗi: Khu vực cha không tồn tại!");
        }

        existing.setParentZoneId(updates.getParentZoneId());
        existing.setZoneName(updates.getZoneName());
        existing.setZoneType(updates.getZoneType());
        existing.setCapacity(updates.getCapacity());

//        existing.setCurrentOccupancy(updates.getCurrentOccupancy());

        return zoneRepository.save(existing);
    }

    @Transactional
    public void deleteZone(Integer id) {
        Zone existing = getZoneById(id);

        if (zoneRepository.existsByParentZoneId(id)) {
            throw new BusinessException("Không thể xóa: Khu vực này đang chứa các khu vực con. Vui lòng xóa khu vực con trước!");
        }

        if (ioTDeviceRepository.existsByZoneId(id)) {
            throw new BusinessException("Không thể xóa: Đang có thiết bị IoT lắp đặt tại khu vực này!");
        }

        if (existing.getCurrentOccupancy() != null && existing.getCurrentOccupancy() > 0) {
            throw new BusinessException("Không thể xóa: Khu vực này hiện đang có xe đỗ!");
        }

        zoneRepository.delete(existing);
    }



}
