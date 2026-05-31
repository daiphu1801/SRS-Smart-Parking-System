package com.smartparking.shared.kafka.consumer.service;

import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.operation.service.system.IoTService;
import com.smartparking.shared.kafka.dto.ParkingSessionEvent;
import com.smartparking.subscription.entity.VehicleType;
import com.smartparking.subscription.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSessionConsumerService {

    private final ParkingSessionRepository sessionRepo;
    private final VehicleTypeRepository vehicleTypeRepo;
    private final ZoneRepository zoneRepo;
    private final IoTService iotService;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "PARKING_ENTRY_TOPIC", groupId = "parking-group")
    public void consumeEntryEvents(List<ConsumerRecord<String, ParkingSessionEvent>> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        log.info("[📥 KAFKA BATCH] Bắt đầu xử lý mẻ Check-in gồm {} xe", records.size());

        List<ParkingSessionEvent> validEvents = new ArrayList<>();
        List<String> vehicleNos = new ArrayList<>();
        List<ParkingSession> batchToSave = new ArrayList<>();

        for (ConsumerRecord<String, ParkingSessionEvent> record : records) {
            ParkingSessionEvent payload = record.value();
            if (payload == null || payload.getVehicleNo() == null) {
                continue;
            }
            validEvents.add(payload);
            vehicleNos.add(payload.getVehicleNo());
        }

        if (validEvents.isEmpty()) return;

        List<ParkingSession> existingSessions = sessionRepo.findByVehicleNoInAndExitTimeIsNull(vehicleNos);

        Set<String> parkedVehicleNos = existingSessions.stream()
                .map(ParkingSession::getVehicleNo)
                .collect(Collectors.toSet());

        for (ParkingSessionEvent payload : validEvents) {
            try {
                String vehicleNo = payload.getVehicleNo();

                if (parkedVehicleNos.contains(vehicleNo)) {
                    log.warn("[⚠️ KAFKA BATCH] Xe {} đã có trong bãi. Bỏ qua lệnh Insert trùng!", vehicleNo);
                    continue;
                }

                parkedVehicleNos.add(vehicleNo);

                LocalDateTime entryTime = payload.getEntryTime() != null
                        ? LocalDateTime.parse(payload.getEntryTime())
                        : LocalDateTime.now();

                VehicleType vType = payload.getVehicleTypeId() != null
                        ? vehicleTypeRepo.getReferenceById(Integer.valueOf(payload.getVehicleTypeId().toString()))
                        : null;

                Zone zoneInObj = payload.getZoneInId() != null
                        ? zoneRepo.getReferenceById(Integer.valueOf(payload.getZoneInId().toString()))
                        : null;

                ParkingSession session = ParkingSession.builder()
                        .vehicleNo(vehicleNo)
                        .entryTime(entryTime)
                        .imageInUrl(payload.getImageInUrl())
                        .vehicleType(vType)
                        .zoneIn(zoneInObj)
                        .bookingDetailId(payload.getBookingDetailId() != null ? Integer.valueOf(payload.getBookingDetailId().toString()) : null)
                        .amountPaid(java.math.BigDecimal.ZERO)
                        .amountLeft(java.math.BigDecimal.ZERO)
                        .amountDue(java.math.BigDecimal.ZERO)
                        .flagManual(false)
                        .build();

                batchToSave.add(session);
            } catch (Exception e) {
                log.error("[❌ KAFKA BATCH] Dữ liệu xe bị lỗi, không thể build Session: {}", e.getMessage());
            }
        }

        if (batchToSave.isEmpty()) return;

        try {
            List<ParkingSession> savedSessions = sessionRepo.saveAll(batchToSave);
            log.info("[✅ KAFKA BATCH] Đã lưu thành công {} xe vào Database!", savedSessions.size());

            for (ParkingSession savedSession : savedSessions) {
                try {
                    String redisKey = iotService.getRedisKey(savedSession.getVehicleNo());
                    ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);

                    if (cachedSession != null) {
                        cachedSession.setId(savedSession.getId());
                        redisTemplate.opsForValue().set(redisKey, cachedSession, Duration.ofHours(24));
                        log.info("[🔄 KAFKA BATCH] Đã cắm SessionID {} vào Redis cho xe {}", savedSession.getId(), savedSession.getVehicleNo());
                    }
                } catch (Exception ex) {
                    log.error("[❌ KAFKA BATCH] Lỗi đồng bộ Redis cho xe {}: {}", savedSession.getVehicleNo(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[❌ KAFKA BATCH] Lỗi khi lưu mẻ dữ liệu vào DB: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "PARKING_EXIT_TOPIC", groupId = "parking-group")
    public void consumeExitEvents(List<ConsumerRecord<String, ParkingSessionEvent>> records) {
        if (records == null || records.isEmpty()) return;

        log.info("[📤 KAFKA BATCH] Bắt đầu xử lý mẻ Check-out gồm {} xe", records.size());

        List<ParkingSessionEvent> validEvents = new ArrayList<>();
        Set<Long> sessionIdsToFetch = new HashSet<>();
        Set<String> fallbackVehicleNos = new HashSet<>();

        for (ConsumerRecord<String, ParkingSessionEvent> record : records) {
            ParkingSessionEvent payload = record.value();
            if (payload == null || payload.getVehicleNo() == null) continue;

            validEvents.add(payload);

            if (payload.getId() != null) {
                sessionIdsToFetch.add(payload.getId());
            } else {
                fallbackVehicleNos.add(payload.getVehicleNo().toString());
            }
        }

        if (validEvents.isEmpty()) return;

        // CHỐNG N+1 QUERY (Lấy data 1 lần)
        Map<Long, ParkingSession> sessionMap = new HashMap<>();
        if (!sessionIdsToFetch.isEmpty()) {
            // Dùng findAllById của JPA để query gom mẻ bằng lệnh IN (...)
            List<ParkingSession> dbSessions = sessionRepo.findAllById(sessionIdsToFetch);
            for (ParkingSession s : dbSessions) {
                sessionMap.put(s.getId(), s);
            }
        }

        // Mò dưới DB những xe bị Camera gửi rớt ID
        Map<String, ParkingSession> fallbackMap = new HashMap<>();
        if (!fallbackVehicleNos.isEmpty()) {
            List<ParkingSession> activeSessions = sessionRepo.findByVehicleNoInAndExitTimeIsNull(new ArrayList<>(fallbackVehicleNos));
            for (ParkingSession s : activeSessions) {
                fallbackMap.put(s.getVehicleNo(), s);
            }
        }

        List<ParkingSession> batchToUpdate = new ArrayList<>();

        for (ParkingSessionEvent payload : validEvents) {
            try {
                String vehicleNo = payload.getVehicleNo().toString();
                ParkingSession sessionToUpdate = null;

                // Ưu tiên lấy theo ID, nếu không có thì lấy theo biển số
                if (payload.getId() != null) {
                    sessionToUpdate = sessionMap.get(payload.getId());
                } else {
                    sessionToUpdate = fallbackMap.get(vehicleNo);
                    if (sessionToUpdate != null) {
                        log.info("[🔍 KAFKA BATCH] Đã vớt thành công xe {} qua biển số dù thiếu SessionID!", vehicleNo);
                    }
                }

                if (sessionToUpdate == null) {
                    log.warn("[⚠️ KAFKA BATCH] Không tìm thấy xe {} trong bãi để chốt giờ ra!", vehicleNo);
                    continue;
                }

                if (payload.getExitTime() != null) {
                    sessionToUpdate.setExitTime(LocalDateTime.parse(payload.getExitTime()));
                } else {
                    sessionToUpdate.setExitTime(LocalDateTime.now());
                }

                if (payload.getZoneOutId() != null) {
                    Zone zoneOutObj = zoneRepo.getReferenceById(Integer.valueOf(payload.getZoneOutId().toString()));
                    sessionToUpdate.setZoneOut(zoneOutObj);
                }

                sessionToUpdate.setImageOutUrl(payload.getImageOutUrl());
                if (payload.getAmountPaid() != null) sessionToUpdate.setAmountPaid(payload.getAmountPaid());
                if (payload.getAmountLeft() != null) sessionToUpdate.setAmountLeft(payload.getAmountLeft());

                batchToUpdate.add(sessionToUpdate);

            } catch (Exception e) {
                log.error("[❌ KAFKA BATCH] Dữ liệu Check-out xe {} bị lỗi: {}", payload.getVehicleNo(), e.getMessage());
            }
        }

        if (batchToUpdate.isEmpty()) return;


        try {
            sessionRepo.saveAll(batchToUpdate);
            log.info("[✅ KAFKA BATCH] Đã chốt sổ giờ RA hàng loạt cho {} xe thành công!", batchToUpdate.size());

            for (ParkingSession s : batchToUpdate) {
                try {
                    String redisKey = iotService.getRedisKey(s.getVehicleNo());
                    redisTemplate.delete(redisKey);
                } catch (Exception ex) {
                    log.error("[⚠️ KAFKA BATCH] Lỗi khi xóa Redis của xe {}: {}", s.getVehicleNo(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[❌ KAFKA BATCH] Lỗi khi lưu mẻ Check-out vào DB: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "PARKING_UPDATE_TOPIC", groupId = "parking-group")
    public void consumeUpdateEvents(List<ConsumerRecord<String, ParkingSessionEvent>> records) {
        if (records == null || records.isEmpty()) return;

        log.info("[🔄 KAFKA BATCH] Bắt đầu xử lý mẻ Update gồm {} xe", records.size());

        List<ParkingSessionEvent> validEvents = new ArrayList<>();
        Set<Long> sessionIdsToFetch = new HashSet<>();
        Set<String> fallbackVehicleNos = new HashSet<>();

        for (ConsumerRecord<String, ParkingSessionEvent> record : records) {
            ParkingSessionEvent payload = record.value();
            if (payload == null || payload.getVehicleNo() == null) continue;

            validEvents.add(payload);
            if (payload.getId() != null) {
                sessionIdsToFetch.add(payload.getId());
            } else {
                fallbackVehicleNos.add(payload.getVehicleNo().toString());
            }
        }

        if (validEvents.isEmpty()) return;

        Map<Long, ParkingSession> sessionMap = new HashMap<>();
        if (!sessionIdsToFetch.isEmpty()) {
            List<ParkingSession> dbSessions = sessionRepo.findAllById(sessionIdsToFetch);
            for (ParkingSession s : dbSessions) {
                sessionMap.put(s.getId(), s);
            }
        }

        Map<String, ParkingSession> fallbackMap = new HashMap<>();
        if (!fallbackVehicleNos.isEmpty()) {
            List<ParkingSession> activeSessions = sessionRepo.findByVehicleNoInAndExitTimeIsNull(new ArrayList<>(fallbackVehicleNos));
            for (ParkingSession s : activeSessions) {
                fallbackMap.put(s.getVehicleNo(), s);
            }
        }

        List<ParkingSession> batchToUpdate = new ArrayList<>();
        Set<Long> processedSessionIds = new HashSet<>();

        for (ParkingSessionEvent payload : validEvents) {
            try {
                String vehicleNo = payload.getVehicleNo().toString();
                ParkingSession sessionToUpdate = null;

                if (payload.getId() != null) {
                    sessionToUpdate = sessionMap.get(payload.getId());
                } else {
                    sessionToUpdate = fallbackMap.get(vehicleNo);
                }

                if (sessionToUpdate == null) {
                    log.warn("[⚠️ KAFKA BATCH] Lệnh Update bị bỏ qua: Không tìm thấy Session ID của xe {} dưới DB!", vehicleNo);
                    continue;
                }

//                if (processedSessionIds.contains(sessionToUpdate.getId())) continue;
//                processedSessionIds.add(sessionToUpdate.getId());

                if (payload.getGracePeriodEnd() != null) {
                    sessionToUpdate.setGracePeriodEnd(LocalDateTime.parse(payload.getGracePeriodEnd()));
                }
                if (payload.getAmountPaid() != null) sessionToUpdate.setAmountPaid(payload.getAmountPaid());
                if (payload.getAmountLeft() != null) sessionToUpdate.setAmountLeft(payload.getAmountLeft());
                if (payload.getAmountDue() != null) sessionToUpdate.setAmountDue(payload.getAmountDue());
                if (payload.getFlagManual() != null) sessionToUpdate.setFlagManual(payload.getFlagManual());

                batchToUpdate.add(sessionToUpdate);

            } catch (Exception e) {
                log.error("[❌ KAFKA BATCH] Dữ liệu Update xe {} bị lỗi: {}", payload.getVehicleNo(), e.getMessage());
            }
        }

        if (batchToUpdate.isEmpty()) return;

        try {
            sessionRepo.saveAll(batchToUpdate);
            log.info("[✅ KAFKA BATCH] Đã CẬP NHẬT hàng loạt thành công cho {} xe!", batchToUpdate.size());
        } catch (Exception e) {
            log.error("[❌ KAFKA BATCH] Lỗi khi lưu mẻ Update vào DB: {}", e.getMessage(), e);
        }
    }

}