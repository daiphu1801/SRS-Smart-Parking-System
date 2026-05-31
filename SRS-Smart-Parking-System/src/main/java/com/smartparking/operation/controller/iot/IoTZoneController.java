package com.smartparking.operation.controller.iot;

import com.smartparking.operation.service.system.IoTZoneService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.service.command.ZoneQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/zone-transition")
@RequiredArgsConstructor
public class IoTZoneController {
    private final IoTZoneService ioTZoneService;

    @PostMapping("/transitions/devices/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> updateZoneTransition(
            @PathVariable Integer deviceId) {
        ioTZoneService.updateZoneTransition(deviceId);

        return ResponseEntity.ok(ApiResponse.success(
                "Đã ghi nhận sự kiện xe qua rào từ thiết bị thành công",
                null
        ));
    }

}
