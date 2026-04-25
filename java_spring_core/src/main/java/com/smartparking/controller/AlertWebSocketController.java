package com.smartparking.controller;

import lombok.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AlertWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/alert/test")
    public void test() {
        messagingTemplate.convertAndSend("/topic/alerts",
            new AlertMessage("TEST", "Connection OK", null, LocalDateTime.now()));
    }

    public void broadcastViolation(String plate, String zone, String alertType) {
        messagingTemplate.convertAndSend("/topic/alerts",
            new AlertMessage(alertType, plate, zone, LocalDateTime.now()));
    }

    @Data
    @AllArgsConstructor
    public static class AlertMessage {
        private String type;
        private String plate;
        private String zone;
        private LocalDateTime timestamp;
    }
}
