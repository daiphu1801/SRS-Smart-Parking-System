package com.smartparking.operation.service.system;

import com.smartparking.operation.dto.response.ZoneTreeResponse;
import com.smartparking.operation.entity.IoTDevice;
import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.IoTDeviceRepository;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.kafka.dto.ZoneTransitionEvent;
import com.smartparking.shared.kafka.producer.service.KafkaZoneProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class IoTZoneService {

    private final IoTDeviceRepository iotDeviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaZoneProducer kafkaZoneProducer;

    public void updateZoneTransition(Integer deviceId) {
        if (deviceId == null) return;

        IoTDevice device = iotDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thiết bị IoT ID: " + deviceId));

        Integer zoneFromId = device.getZoneIdFrom();
        Integer zoneToId = device.getZoneIdTo();

        if (zoneFromId != null) {
            redisTemplate.opsForValue().decrement("parking:zone:" + zoneFromId + ":count");
        }
        if (zoneToId != null) {
            redisTemplate.opsForValue().increment("parking:zone:" + zoneToId + ":count");
        }

        ZoneTransitionEvent event = new ZoneTransitionEvent(
                device.getId(),
                zoneFromId,
                zoneToId,
                System.currentTimeMillis()
        );

        kafkaZoneProducer.sendZoneTransition(event);
    }


}
