package com.smartparking.operation.controller.iot;

import com.smartparking.operation.dto.request.IotEntryRequest;
import com.smartparking.operation.dto.request.IotExitRequest;
import com.smartparking.operation.service.system.IoTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/parking")
@RequiredArgsConstructor
public class IoTSessionController {

    private final IoTService iotService;

    @PostMapping("/entry")
    public ResponseEntity<?> handleEntry(@RequestBody IotEntryRequest request) {
        return ResponseEntity.ok(iotService.handleEntry(request));
    }

    @PutMapping("/exit")
    public ResponseEntity<?> handleExit(@RequestBody IotExitRequest request) {
        return ResponseEntity.ok(iotService.handleExit(request));
    }
}
