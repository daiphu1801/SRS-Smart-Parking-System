package com.smartparking.shared.service;

import com.smartparking.shared.dto.SystemConfigResponse;
import com.smartparking.shared.dto.SystemConfigUpdateRequest;
import com.smartparking.shared.entity.SystemConfig;
import com.smartparking.shared.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_CONFIG_PREFIX = "parking:config:";

    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SystemConfigResponse updateConfig(Integer id, SystemConfigUpdateRequest request) {
        SystemConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình hệ thống yêu cầu"));

        config.setConfigValue(request.getConfigValue());
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        SystemConfig savedConfig = configRepository.save(config);

        String redisKey = REDIS_CONFIG_PREFIX + savedConfig.getConfigKey();
        redisTemplate.opsForValue().set(redisKey, savedConfig.getConfigValue());

        return mapToResponse(savedConfig);
    }

    private SystemConfigResponse mapToResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    public String getConfigValue(String configKey, String defaultValue) {
        String redisKey = REDIS_CONFIG_PREFIX + configKey;

        String cachedValue = redisTemplate.opsForValue().get(redisKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        log.warn("[⚠️ CACHE MISS] Mất cache cấu hình {}, tiến hành gọi Database...", configKey);
        return configRepository.findByConfigKey(configKey)
                .map(config -> {
                    redisTemplate.opsForValue().set(redisKey, config.getConfigValue());
                    return config.getConfigValue();
                })
                .orElse(defaultValue);
    }

    public Integer getGracePeriodMinutes() {
        return Integer.parseInt(getConfigValue("PAYMENT_GRACE_PERIOD_MINUTES", "15"));
    }

    public Integer getMonthlyVehicleBuffer() {
        return Integer.parseInt(getConfigValue("MONTHLY_VEHICLE_BUFFER_SLOTS", "50"));
    }
}