package com.smartparking.shared.kafka;

import com.smartparking.shared.config.KafkaConfig;
import com.smartparking.operation.service.SessionProcessingService;
import com.smartparking.shared.kafka.dto.VehicleEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleExitConsumer {

    private final SessionProcessingService sessionService;

    @KafkaListener(topics = KafkaConfig.VEHICLE_EXIT, groupId = "smart-parking-core")
    public void consume(VehicleEvent event) {
        log.info("Received vehicle-exit: {}", event);
        sessionService.processVehicleExit(event);
    }
}
