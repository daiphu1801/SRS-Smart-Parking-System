package com.smartparking.kafka;

import com.smartparking.config.KafkaConfig;
import com.smartparking.kafka.dto.BarrierCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BarrierCommandProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCommand(BarrierCommand command) {
        log.info("Sending barrier-command: {}", command);
        kafkaTemplate.send(KafkaConfig.BARRIER_CMD, command.getDeviceId(), command);
    }
}
