package com.smartparking.shared.startup;

import com.smartparking.shared.entity.SystemConfig;
import com.smartparking.shared.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigCacheInitializer {

    private final SystemConfigRepository configRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    private static final String REDIS_CONFIG_PREFIX = "parking:config:";
    private static final String INIT_FLAG_KEY = "parking:system:config_initialized";
    private static final String INIT_LOCK_KEY = "lock:system:config_init";

    @EventListener(ApplicationReadyEvent.class)
    public void initConfigCacheOnStartup() {
        RLock lock = redissonClient.getLock(INIT_LOCK_KEY);

        try {
            boolean isLeader = lock.tryLock(0, 30, TimeUnit.SECONDS);

            if (!isLeader) {
                log.info("[ K8s INIT] Pod này chậm chân hơn. Bỏ qua việc nạp Cache Cấu hình Hệ thống!");
                return;
            }

            log.info("[ K8s INIT - LEADER] Pod hiện tại làm Thủ lĩnh! Bắt đầu nạp System Config...");

            if (Boolean.TRUE.equals(redisTemplate.hasKey(INIT_FLAG_KEY))) {
                log.info("Cache Cấu hình đã được nạp từ trước. Thủ lĩnh huỷ lệnh càn quét DB.");
                return;
            }

            List<SystemConfig> allConfigs = configRepository.findAll();

            if (allConfigs.isEmpty()) {
                log.warn("[⚠️ K8s INIT] Không có cấu hình hệ thống nào trong Database!");
                return;
            }

            for (SystemConfig config : allConfigs) {
                String redisKey = REDIS_CONFIG_PREFIX + config.getConfigKey();
                redisTemplate.opsForValue().set(redisKey, config.getConfigValue());
            }

            redisTemplate.opsForValue().set(INIT_FLAG_KEY, "true");

            log.info("[ K8s INIT - LEADER] Đã nạp đạn thành công {} Cấu hình lên Redis!", allConfigs.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ K8s INIT] Pod bị gián đoạn khi đang xin Cờ khởi tạo cấu hình: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}