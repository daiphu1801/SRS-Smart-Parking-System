package com.smartparking.operation.controller.admin;

import com.smartparking.operation.dto.response.ZoneTreeResponse;
import com.smartparking.operation.entity.*;
import com.smartparking.operation.service.admin.AdminZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/zones")
@RequiredArgsConstructor
public class AdminZoneController {
    private final AdminZoneService adminZoneService;
    @PreAuthorize("hasAuthority('ZONE_READ')")
    @GetMapping("tree")
    public ResponseEntity<List<ZoneTreeResponse>> getZoneTree() {
        return ResponseEntity.ok(adminZoneService.getZoneTree());
    }

    @PreAuthorize("hasAuthority('ZONE_READ')")
    @GetMapping()
    public ResponseEntity<List<Zone>> getZones() {
        return ResponseEntity.ok(adminZoneService.getZones());
    }

    @PreAuthorize("hasAuthority('ZONE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZoneById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminZoneService.getZoneById(id));
    }

    @PreAuthorize("hasAuthority('ZONE_CREATE')")
    @PostMapping()
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.status(201).body(adminZoneService.createZone(zone));
    }

    @PreAuthorize("hasAuthority('ZONE_UPDATE')")
    @PutMapping("{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Integer id, @RequestBody Zone updates) {
        return ResponseEntity.ok(adminZoneService.updateZone(id, updates));
    }

    @PreAuthorize("hasAuthority('ZONE_DELETE')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Integer id) {
        adminZoneService.deleteZone(id);
        return ResponseEntity.ok().build();
    }

}
