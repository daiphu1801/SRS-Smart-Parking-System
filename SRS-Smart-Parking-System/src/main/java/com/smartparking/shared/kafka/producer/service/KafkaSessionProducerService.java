package com.smartparking.shared.kafka.producer.service;

import com.smartparking.shared.kafka.dto.ParkingSessionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSessionProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEntryEvent(String vehicleNo, ParkingSessionEvent eventPayload) {
        kafkaTemplate.send("PARKING_ENTRY_TOPIC", vehicleNo, eventPayload);
        log.info("[🚀 KAFKA PRODUCER] Đã bắn event Check-in của xe {} lên Topic PARKING_ENTRY_TOPIC", vehicleNo);
    }

    public void sendExitEvent(String vehicleNo, ParkingSessionEvent eventPayload) {
        kafkaTemplate.send("PARKING_EXIT_TOPIC", vehicleNo, eventPayload);
        log.info("[🚀 KAFKA PRODUCER] Đã bắn event Check-out của xe {} lên Topic PARKING_EXIT_TOPIC", vehicleNo);
    }

    // 3. Luồng Cập nhật (Thu tiền mặt, đồng bộ Cache...)
    public void sendUpdateEvent(String vehicleNo, ParkingSessionEvent eventPayload) {
        kafkaTemplate.send("PARKING_UPDATE_TOPIC", vehicleNo, eventPayload);
        log.info("[🚀 KAFKA PRODUCER] Đã bắn event Update của xe {} lên Topic PARKING_UPDATE_TOPIC", vehicleNo);
    }
}