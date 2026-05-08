package com.smartparking.operation.service;

import com.smartparking.operation.repository.IoTDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IoTService {

    private final IoTDeviceRepository deviceRepo;

    public void processEntry(Map<String, Object> data) {
        log.info("Processing entry from IoT: {}", data);
    }

    public void processExit(Map<String, Object> data) {
        log.info("Processing exit from IoT: {}", data);
    }

    public void processZoneTransition(Map<String, Object> data) {
        log.info("Processing zone transition: {}", data);
    }

    public void pingDevice(String deviceCode) {
        log.info("Ping from device: {}", deviceCode);
        // Note: IoTDevice entity doesn't have deviceCode right now, so we log it
    }

    public String handleImageUpload(byte[] imageBytes) {
        log.info("Image uploaded, size: {} bytes", imageBytes.length);
        return "https://dummy-url.com/image.jpg";
    }
}
