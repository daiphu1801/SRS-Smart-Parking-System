package com.smartparking.controller;

import com.smartparking.entity.DeviceStatus;
import com.smartparking.entity.IoTDevice;
import com.smartparking.entity.Zone;
import com.smartparking.repository.IoTDeviceRepository;
import com.smartparking.repository.ZoneRepository;
import com.smartparking.service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TopologyController {

    private final ZoneRepository zoneRepo;
    private final IoTDeviceRepository deviceRepo;
    private final NavigationService navigationService;

    @GetMapping("/api/zones")
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(navigationService.getZoneTree());
    }

    @PostMapping("/api/admin/zones")
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.ok(zoneRepo.save(zone));
    }

    @GetMapping("/api/admin/zones/{id}/occupancy")
    public ResponseEntity<?> getOccupancy(@PathVariable Integer id) {
        return zoneRepo.findById(id).map(z -> ResponseEntity.ok((Object) Map.of(
            "zoneId", id, "zoneName", z.getZoneName(),
            "capacity", z.getCapacity(), "occupancy", z.getCurrentOccupancy()
        ))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/admin/devices")
    public ResponseEntity<List<IoTDevice>> getAllDevices() {
        return ResponseEntity.ok(deviceRepo.findAll());
    }

    @PostMapping("/api/admin/devices")
    public ResponseEntity<IoTDevice> createDevice(@RequestBody IoTDevice device) {
        return ResponseEntity.ok(deviceRepo.save(device));
    }

    @PutMapping("/api/admin/devices/{id}/status")
    public ResponseEntity<?> updateDeviceStatus(@PathVariable Integer id,
                                                @RequestBody Map<String, String> body) {
        return deviceRepo.findById(id).map(d -> {
            d.setStatus(DeviceStatus.valueOf(body.get("status")));
            d.setLastPing(LocalDateTime.now());
            return ResponseEntity.ok((Object) deviceRepo.save(d));
        }).orElse(ResponseEntity.notFound().build());
    }
}
