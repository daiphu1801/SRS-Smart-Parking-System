package com.smartparking.shared.kafka.consumer.service;

import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.shared.kafka.dto.ZoneTransitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaZoneConsumer {

    private final ZoneRepository zoneRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "ZONE_TRANSITION_TOPIC", groupId = "parking-zone-group")
    @Transactional
    public void consumeZoneTransitions(List<ConsumerRecord<String, ZoneTransitionEvent>> records) {
        if (records == null || records.isEmpty()) return;

        log.info("[📥 KAFKA BATCH] Bắt đầu xử lý mẻ chuyển Zone gồm {} sự kiện", records.size());

        // 1. Gom nhóm cộng trừ (Map-Reduce trên RAM)
        Map<Integer, Integer> zoneOccupancyChanges = new HashMap<>();

        for (ConsumerRecord<String, ZoneTransitionEvent> record : records) {
            ZoneTransitionEvent event = record.value();
            if (event == null) continue;

            if (event.getZoneFromId() != null) {
                zoneOccupancyChanges.merge(event.getZoneFromId(), -1, Integer::sum);
            }
            if (event.getZoneToId() != null) {
                zoneOccupancyChanges.merge(event.getZoneToId(), 1, Integer::sum);
            }
        }

        if (zoneOccupancyChanges.isEmpty()) return;

        // 2. Chạy UPDATE nguyên tử (An toàn tuyệt đối chống đụng độ)
        for (Map.Entry<Integer, Integer> entry : zoneOccupancyChanges.entrySet()) {
            if (entry.getValue() != 0) {
                try {
                    zoneRepository.updateOccupancy(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    log.error("[❌ KAFKA BATCH] Lỗi cập nhật Zone {}: {}", entry.getKey(), e.getMessage());
                }
            }
        }

        // 3. TỐI ƯU SELECT: Lấy tất cả số xe của các Zone vừa bị thay đổi TRONG 1 CÂU QUERY
        syncMultipleZonesToRedis(zoneOccupancyChanges.keySet());

        log.info("[✅ KAFKA BATCH] Đã chốt sổ thay đổi Zone thành công!");
    }

    private void syncMultipleZonesToRedis(Set<Integer> zoneIds) {
        if (zoneIds == null || zoneIds.isEmpty()) return;

        // Cần viết thêm hàm này trong Repository để query bằng IN (...)
        List<Zone> updatedZones = zoneRepository.findAllById(zoneIds);

        // Đẩy lên Redis (Có thể dùng Pipeline của Redis để tối ưu hơn nữa, nhưng vòng lặp set trên RAM là đủ nhanh)
        for (Zone zone : updatedZones) {
            String redisKey = "parking:zone:" + zone.getId() + ":count";
            redisTemplate.opsForValue().set(redisKey, zone.getCurrentOccupancy());
            log.debug("[🔄 CACHE SYNCED] Đồng bộ Zone {}: {} xe", zone.getId(), zone.getCurrentOccupancy());
        }
    }
}