package com.smartparking.operation.controller.admin;

import com.smartparking.operation.entity.IoTDevice;
import com.smartparking.operation.service.admin.AdminDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/devices")
@RequiredArgsConstructor
public class AdminDeviceController {
    private final AdminDeviceService adminDeviceService;

    @PreAuthorize("hasAuthority('DEVICE_READ')")
    @GetMapping()
    public ResponseEntity<List<IoTDevice>> listDevices() {
        // Sau này có thể thêm phân trang, filter trạng thái Online/Offline vào đây
        return ResponseEntity.ok(adminDeviceService.getAllDevices());
    }
    @PreAuthorize("hasAuthority('DEVICE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<IoTDevice> getDeviceById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminDeviceService.getDeviceById(id));
    }
    @PreAuthorize("hasAuthority('DEVICE_CREATE')")
    @PostMapping
    public ResponseEntity<IoTDevice> createDevice(@RequestBody IoTDevice device) {
        return ResponseEntity.status(201).body(adminDeviceService.createDevice(device));
    }
    @PreAuthorize("hasAuthority('DEVICE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<IoTDevice> updateDevice(@PathVariable Integer id, @RequestBody IoTDevice updates) {
        return ResponseEntity.ok(adminDeviceService.updateDevice(id, updates));
    }
    @PreAuthorize("hasAuthority('DEVICE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable Integer id) {
        adminDeviceService.deleteDevice(id);
        return ResponseEntity.ok("Xóa thiết bị thành công");
    }

//    // --- Lệnh điều khiển Barrier ---
//@PreAuthorize("hasAuthority('DEVICE_CONTROL')")
//    @PostMapping("/{id}/barrier-control")
//    public ResponseEntity<?> controlBarrier(@PathVariable Integer id, @RequestBody Map<String, String> body) {
//        // TODO: Chỗ này sau này sẽ gọi MQTT hoặc HTTP Request đẩy lệnh xuống thiết bị thật
//        return ResponseEntity.ok(Map.of("message", "Đã gửi lệnh [" + body.get("command") + "] xuống barrier ID: " + id));
//    }
    @PreAuthorize("hasAuthority('DEVICE_READ')")
    @GetMapping("/zones/{id}")
    public ResponseEntity<List<IoTDevice>> getDevicesByZone(@PathVariable Integer id) {
        // Lấy tất cả Device có zoneIdFrom = id HOẶC zoneIdTo = id
        return ResponseEntity.ok(adminDeviceService.getDevicesByZone(id));
    }
    
}
