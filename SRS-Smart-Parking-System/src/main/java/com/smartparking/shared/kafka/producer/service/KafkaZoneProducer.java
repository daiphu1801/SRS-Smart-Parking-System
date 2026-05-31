package com.smartparking.shared.kafka.producer.service;

import com.smartparking.shared.kafka.dto.ZoneTransitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaZoneProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendZoneTransition(ZoneTransitionEvent event) {
        if (event == null ) return;

        String partitionKey = String.valueOf(event.getDeviceId());
        kafkaTemplate.send("ZONE_TRANSITION_TOPIC", partitionKey, event);


        log.info("[🚀 KAFKA PRODUCER] Đã bắn mẻ {} event chuyển Zone lên Topic ZONE_TRANSITION_TOPIC", event.getDeviceId());
    }
}