package com.smartparking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * Hot-reload settings from settings.json.
 * Admin gọi PUT /api/admin/settings → service ghi file + reload vào RAM.
 * Không cần restart server.
 */
@Service
@Slf4j
public class SettingsService {

    @Value("${app.settings.file-path}")
    private String settingsFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter @Setter
    private int gracePeriodMinutes = 15;
    @Getter @Setter
    private int safetyBufferPercent = 4;
    @Getter @Setter
    private int alertThresholdSeconds = 240;
    @Getter @Setter
    private int paymentQrExpirySeconds = 300;
    @Getter @Setter
    private int guestCutoffBufferSlots = 10;
    @Getter @Setter
    private double lprConfidenceThreshold = 0.80;

    @PostConstruct
    public void loadOnStartup() {
        reload();
    }

    @SuppressWarnings("unchecked")
    public void reload() {
        try {
            File file = new File(settingsFilePath);
            if (!file.exists()) {
                log.warn("settings.json not found at {}. Using defaults.", settingsFilePath);
                return;
            }
            Map<String, Object> map = objectMapper.readValue(file, Map.class);
            if (map.containsKey("grace_period_minutes"))
                gracePeriodMinutes = (int) map.get("grace_period_minutes");
            if (map.containsKey("overbooking_safety_buffer_percent"))
                safetyBufferPercent = (int) map.get("overbooking_safety_buffer_percent");
            if (map.containsKey("alert_threshold_seconds"))
                alertThresholdSeconds = (int) map.get("alert_threshold_seconds");
            if (map.containsKey("payment_qr_expiry_seconds"))
                paymentQrExpirySeconds = (int) map.get("payment_qr_expiry_seconds");
            if (map.containsKey("lpr_confidence_threshold"))
                lprConfidenceThreshold = ((Number) map.get("lpr_confidence_threshold")).doubleValue();
            log.info("Settings loaded: gracePeriod={}m, buffer={}%", gracePeriodMinutes, safetyBufferPercent);
        } catch (Exception e) {
            log.error("Failed to load settings.json: {}", e.getMessage());
        }
    }

    public void saveAndReload(Map<String, Object> newSettings) {
        try {
            File file = new File(settingsFilePath);
            Map<String, Object> current = objectMapper.readValue(file, Map.class);
            current.putAll(newSettings);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, current);
            reload();
        } catch (Exception e) {
            log.error("Failed to save settings: {}", e.getMessage());
            throw new RuntimeException("Could not save settings: " + e.getMessage());
        }
    }
}
