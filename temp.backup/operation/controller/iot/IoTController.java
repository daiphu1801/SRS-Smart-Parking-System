package com.smartparking.operation.controller.iot;

import com.smartparking.operation.service.IoTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class IoTController {

    private final IoTService iotService;

    @PostMapping("/api/v1/system/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = iotService.handleImageUpload(file.getBytes());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/api/v1/iot/parking/entry")
    public ResponseEntity<?> handleEntry(@RequestBody Map<String, Object> data) {
        iotService.processEntry(data);
        return ResponseEntity.ok(Map.of("message", "Entry processed", "command", "OPEN_BARRIER"));
    }

    @PostMapping("/api/v1/iot/parking/exit")
    public ResponseEntity<?> handleExit(@RequestBody Map<String, Object> data) {
        iotService.processExit(data);
        return ResponseEntity.ok(Map.of("message", "Exit processed"));
    }

    @PostMapping("/api/v1/iot/parking/zone-transition")
    public ResponseEntity<?> handleTransition(@RequestBody Map<String, Object> data) {
        iotService.processZoneTransition(data);
        return ResponseEntity.ok(Map.of("message", "Transition processed"));
    }

    @PostMapping("/api/v1/iot/devices/{deviceCode}/ping")
    public ResponseEntity<?> ping(@PathVariable String deviceCode) {
        iotService.pingDevice(deviceCode);
        return ResponseEntity.ok(Map.of("message", "Ping received"));
    }
}
