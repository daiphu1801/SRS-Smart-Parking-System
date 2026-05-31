package com.smartparking.shared.service.command;

import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneQueryService {

    private final ZoneRepository zoneRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    // API của Bảo vệ/Admin sẽ gọi thẳng vào hàm này
    public Integer getZoneOccupancy(Integer zoneId) {
        String redisKey = "parking:zone:" + zoneId + ":count";

        // ==========================================
        // BƯỚC 1: ĐỌC CACHE BÌNH THƯỜNG (99% ăn ở đây)
        // ==========================================
        Object cachedValue = redisTemplate.opsForValue().get(redisKey);
        if (cachedValue != null) {
            return (Integer) cachedValue;
        }

        // ==========================================
        // BƯỚC 2: CACHE SẬP! BẮT ĐẦU TRANH CỜ
        // ==========================================
        log.warn("[⚠️ CACHE MISS] Cache Zone {} mất! Các Pod đang giành Cờ...", zoneId);
        String lockKey = "lock:zone:" + zoneId + ":rebuild";
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLockAcquired = lock.tryLock(2, 5, TimeUnit.SECONDS);

            if (isLockAcquired) {
                log.info("[🚩 THỦ LĨNH] Pod này đã cầm Cờ! Đang chọc DB để vá Cache cho Zone {}", zoneId);

                List<Zone> allZones = zoneRepository.findAll();
                Integer requestedCount = 0;

                if (!allZones.isEmpty()) {
                    for (Zone zone : allZones) {
                        redisTemplate.opsForValue().set("parking:zone:" + zone.getId() + ":count", zone.getCurrentOccupancy());

                        if (zone.getId().equals(zoneId)) {
                            requestedCount = zone.getCurrentOccupancy();
                        }
                    }
                    log.info("[✅ CẤP CỨU THÀNH CÔNG] Đã nạp lại {} Zone lên Redis!", allZones.size());
                }

                return requestedCount;

            } else {
                log.info("[💤 ĐỢI CHỜ] Pod khác đang nạp Cache rồi. Sleep 100ms chờ ăn sẵn...");
                Thread.sleep(100);

                Object recheckedValue = redisTemplate.opsForValue().get(redisKey);
                if (recheckedValue != null) {
                    return (Integer) recheckedValue;
                } else {
                    return zoneRepository.findCurrentOccupancyById(zoneId);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi hệ thống khi đang giành Cờ Redis", e);
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}