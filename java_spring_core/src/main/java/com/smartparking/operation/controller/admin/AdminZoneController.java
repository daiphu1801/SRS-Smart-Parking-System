package com.smartparking.operation.controller.admin;

import com.smartparking.operation.dto.response.ZoneTreeResponse;
import com.smartparking.operation.entity.*;
import com.smartparking.operation.service.admin.AdminZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/zones")
@RequiredArgsConstructor
public class AdminZoneController {
    private final AdminZoneService adminZoneService;

    @GetMapping()
    public ResponseEntity<List<ZoneTreeResponse>> getZoneTree() {
        return ResponseEntity.ok(adminZoneService.getZoneTree());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZoneById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminZoneService.getZoneById(id));
    }

    @PostMapping()
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.status(201).body(adminZoneService.createZone(zone));
    }

    @PutMapping("{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Integer id, @RequestBody Zone updates) {
        return ResponseEntity.ok(adminZoneService.updateZone(id, updates));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteZone(@PathVariable Integer id) {
        adminZoneService.deleteZone(id);
        return ResponseEntity.ok("Xóa khu vực thành công");
    }

    // --- API ĐẶC BIỆT: Lấy thiết bị theo Zone ---

    
}
