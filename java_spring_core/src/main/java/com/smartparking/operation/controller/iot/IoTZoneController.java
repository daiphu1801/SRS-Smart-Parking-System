package com.smartparking.operation.controller.iot;

import com.smartparking.operation.entity.IoTDevice;
import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.service.admin.AdminZoneService;
import com.smartparking.operation.service.system.IoTZoneService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/v1/iot/zone-transition")
@RequiredArgsConstructor
public class IoTZoneController {
    private final IoTZoneService ioTZoneService;

    @PutMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> updateZoneTransition(
            @PathVariable Integer deviceId) {

        ioTZoneService.updateZoneTransition(deviceId);

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật số lượng xe qua lại giữa các khu vực thành công",
                null // Không cần trả data về nữa vì frontend chỉ cần biết thành công hay không
        ));
    }


}
