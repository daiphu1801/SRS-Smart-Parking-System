package com.smartparking.shared.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class DiscordNotificationService {

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String message) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Discord Webhook URL not configured. Cannot send message: {}", message);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> body = Map.of("content", message);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("Successfully sent message to Discord");
        } catch (Exception e) {
            log.error("Failed to send message to Discord", e);
        }
    }
}
