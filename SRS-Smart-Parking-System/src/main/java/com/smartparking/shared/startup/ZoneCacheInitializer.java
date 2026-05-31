package com.smartparking.shared.startup;

import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZoneCacheInitializer {

    private final ZoneRepository zoneRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    @EventListener(ApplicationReadyEvent.class)
    public void initZoneCacheOnStartup() {
        String initLockKey = "lock:system:zone_init";
        RLock lock = redissonClient.getLock(initLockKey);

        try {
            boolean isLeader = lock.tryLock(0, 30, TimeUnit.SECONDS);

            if (!isLeader) {
                log.info("[💤 K8s INIT] Pod này chậm chân hơn Pod khác. Bỏ qua việc nạp Cache Zone!");
                return;
            }

            log.info("[🚀 K8s INIT - LEADER] Pod hiện tại làm Thủ lĩnh! Bắt đầu càn quét DB nạp Cache...");

            if (Boolean.TRUE.equals(redisTemplate.hasKey("parking:system:zone_initialized"))) {
                log.info("[✅ K8s INIT] Cache đã được nạp từ trước. Thủ lĩnh huỷ lệnh càn quét DB.");
                return;
            }

            List<Zone> allZones = zoneRepository.findAll();

            if (allZones.isEmpty()) {
                log.warn("[⚠️ K8s INIT] Không có Zone nào trong Database!");
                return;
            }

            int totalCurrentOccupancy = 0;
            int totalMaxOccupancy = 0;

            for (Zone zone : allZones) {
                String redisKey = "parking:zone:" + zone.getId() + ":count";
                int current = zone.getCurrentOccupancy() != null ? zone.getCurrentOccupancy() : 0;
                int max = zone.getCapacity() != null ? zone.getCapacity() : 0;
                redisTemplate.opsForValue().set(redisKey, zone.getCurrentOccupancy());
                totalCurrentOccupancy += current;
                totalMaxOccupancy += max;
            }

            redisTemplate.opsForValue().set("parking:system:total_current_occupancy", totalCurrentOccupancy);
            redisTemplate.opsForValue().set("parking:system:total_max_occupancy", totalMaxOccupancy);
            redisTemplate.opsForValue().set("parking:system:zone_initialized", "true");

            log.info("[✅ K8s INIT - LEADER] Đã nạp đạn thành công {} Zone lên Redis!", allZones.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[❌ K8s INIT] Pod bị gián đoạn khi đang xin Cờ khởi tạo: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}