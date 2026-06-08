package com.smartparking.operation.service.system;

import com.smartparking.operation.dto.request.IotEntryRequest;
import com.smartparking.operation.dto.request.IotExitRequest;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.payment.service.BillingService;
import com.smartparking.shared.kafka.dto.ParkingSessionEvent;
import com.smartparking.shared.kafka.producer.service.KafkaSessionProducerService;
import com.smartparking.shared.service.SystemConfigService;
import com.smartparking.subscription.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IoTService {

    private final ParkingSessionRepository sessionRepo;
    private final BookingDetailRepository bookingDetailRepo;
    private final ZoneRepository zoneRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final IoTZoneService iotZoneService;
    private final BillingService billingService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaSessionProducerService kafkaSessionProducerService;
    private final SystemConfigService systemConfigService;


    public ParkingSessionEvent syncSessionToCache(ParkingSession session) {
        String redisKey = getRedisKey(session.getVehicleNo());

        ParkingSessionEvent event = ParkingSessionEvent.builder()
                .id(session.getId())
                .vehicleNo(session.getVehicleNo())

                .entryTime(session.getEntryTime() != null ? session.getEntryTime().toString() : null)
                .exitTime(session.getExitTime() != null ? session.getExitTime().toString() : null)
                .gracePeriodEnd(session.getGracePeriodEnd() != null ? session.getGracePeriodEnd().toString() : null)

                .amountPaid(session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO)
                .amountLeft(session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO)
                .amountDue(session.getAmountDue() != null ? session.getAmountDue() : BigDecimal.ZERO)

                .bookingDetailId(session.getBookingDetailId())
                .vehicleTypeId(session.getVehicleTypeId())
                .zoneInId(session.getZoneInId())
                .zoneOutId(session.getZoneOutId())

                .imageInUrl(session.getImageInUrl())
                .imageOutUrl(session.getImageOutUrl())
                .flagManual(session.getFlagManual() != null ? session.getFlagManual() : false)
                .build();

        redisTemplate.opsForValue().set(redisKey, event, Duration.ofHours(24));

        log.info("[CACHE SYNC] Đã đồng bộ Full State của xe {} lên Redis thành công.", session.getVehicleNo());
        kafkaSessionProducerService.sendUpdateEvent(session.getVehicleNo(), event);
        log.info("[🚀 KAFKA SYNC] Đã bắn event Update của xe {} lên Kafka.", session.getVehicleNo());
        return event;
    }

    public String getRedisKey(String licensePlate) {
        return "parking:session:" + licensePlate;
    }

    @Transactional
    public Map<String, Object> handleEntry(IotEntryRequest request) {
        String vehicleNo = request.getVehicleNo();
        String redisKey = getRedisKey(vehicleNo);

        ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);
        if (cachedSession != null) {
            if (cachedSession.getExitTime() == null) {
                log.warn("[CACHE HIT] Xe {} đã ở trong bãi, bỏ qua check-in đúp.", vehicleNo);
                return Map.of("message", "Xe đã ở trong bãi", "command", "OPEN_BARRIER");
            }
        }


        LocalDateTime now = LocalDateTime.now();
        String safePlate = vehicleNo.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        long timestamp = java.time.Instant.now().toEpochMilli();
        String assignedImageName = "checkin_" + safePlate + "_" + timestamp + ".jpg";
        BookingDetail activeBooking = bookingDetailRepo
                .findFirstByVehicleNoAndStatus(request.getVehicleNo(), BookingStatus.ACTIVE)
                .orElse(null);

        if (activeBooking == null) {
            Object currentObj = redisTemplate.opsForValue().get("parking:system:total_current_occupancy");
            Object maxObj = redisTemplate.opsForValue().get("parking:system:total_max_occupancy");

            int totalCurrent = currentObj != null ? Integer.parseInt(String.valueOf(currentObj)) : 0;
            int totalMax = maxObj != null ? Integer.parseInt(String.valueOf(maxObj)) : 0;

            int bufferSlots = systemConfigService.getMonthlyVehicleBuffer();

            int allowedMaxForGuests = totalMax - bufferSlots;

            if (totalCurrent >= allowedMaxForGuests) {
                log.warn("[⛔ REJECT] Bãi đầy. Hiện tại: {}, Ngưỡng vãng lai: {}. Từ chối xe {}",
                        totalCurrent, allowedMaxForGuests, vehicleNo);
                return Map.of(
                        "message", "Bãi xe đã đầy (Chỉ nhận xe tháng)",
                        "command", "KEEP_CLOSED",

                        "Plate number", request.getVehicleNo(),
                        "type", "GUEST_REJECTED"
                );
            }
        }


        Integer typeId = request.getVehicleTypeId();
        if (typeId == null) {
            typeId = guessVehicleTypeIdFromPlate(request.getVehicleNo());
        }

        ParkingSessionEvent event = ParkingSessionEvent.builder()
                .id(null)
                .vehicleNo(vehicleNo)
                .vehicleTypeId(typeId)
                .zoneInId(request.getZoneId())
                .entryTime(now.toString())
                .imageInUrl(request.getImageUrl())
                .bookingDetailId(activeBooking != null ? activeBooking.getId() : null)
                .exitTime(null)
                .imageInUrl(assignedImageName)
                .amountPaid(BigDecimal.ZERO)
                .amountLeft(BigDecimal.ZERO)
                .amountDue(BigDecimal.ZERO)
                .flagManual(false)
                .build();

        redisTemplate.opsForValue().set(redisKey, event, Duration.ofHours(24));

        kafkaSessionProducerService.sendEntryEvent(vehicleNo, event);

        if (log.isDebugEnabled()) {
            log.debug("[💾 REDIS PUT] Đã đẩy bản nháp xe {} lên mây. Chờ Kafka consumer nhặt hàng.", vehicleNo);
        }

        if (request.getZoneId() != null) {
            iotZoneService.updateZoneTransition(request.getDeviceId());
        }

        redisTemplate.opsForValue().increment("parking:system:total_current_occupancy", 1);
        return Map.of(
                "message", "Entry processed successfully",
                "command", "OPEN_BARRIER",
                "Plate number", request.getVehicleNo(),
                "type", activeBooking != null ? "SUBSCRIBER" : "GUEST",
                "assignedImageName", assignedImageName
        );
    }

    @Transactional
    public Map<String, Object> handleExit(IotExitRequest request) {
        String vehicleNo = request.getVehicleNo();
        String redisKey = getRedisKey(vehicleNo);
        LocalDateTime now = LocalDateTime.now();

        ParkingSession session = null;
        ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);
        boolean isFromCache = false;

        if (cachedSession != null && cachedSession.getExitTime() == null) {
            isFromCache = true;

            LocalDateTime entryTime = cachedSession.getEntryTime() != null ? LocalDateTime.parse(cachedSession.getEntryTime()) : null;
            LocalDateTime gracePeriodEnd = cachedSession.getGracePeriodEnd() != null ? LocalDateTime.parse(cachedSession.getGracePeriodEnd()) : null;
            BigDecimal amountLeft = cachedSession.getAmountLeft() != null ? new BigDecimal(cachedSession.getAmountLeft().toString()) : BigDecimal.ZERO;
            BigDecimal amountPaid = cachedSession.getAmountPaid() != null ? new BigDecimal(cachedSession.getAmountPaid().toString()) : BigDecimal.ZERO;
            BigDecimal amountDue = cachedSession.getAmountDue() != null ? new BigDecimal(cachedSession.getAmountDue().toString()) : BigDecimal.ZERO;

            Long cachedSessionId = cachedSession.getId() != null ? Long.valueOf(cachedSession.getId().toString()) : null;

            session = ParkingSession.builder()
                    .id(cachedSessionId)
                    .bookingDetailId(cachedSession.getBookingDetailId())
                    .vehicleNo(cachedSession.getVehicleNo())
                    .vehicleTypeId(cachedSession.getVehicleTypeId())
                    .zoneInId(cachedSession.getZoneInId())
                    .entryTime(entryTime)
                    .gracePeriodEnd(gracePeriodEnd)
                    .imageInUrl(cachedSession.getImageInUrl())
                    .amountDue(amountDue)
                    .amountPaid(amountPaid)
                    .amountLeft(amountLeft)
                    .flagManual(cachedSession.getFlagManual() != null ? cachedSession.getFlagManual() : false)
                    .build();
        } else {
            Optional<ParkingSession> sessionOpt = sessionRepo.findOpenSession(vehicleNo);
            if (sessionOpt.isEmpty()) {
                log.warn("[🚫 DENY] Xe {} ra cổng nhưng không tìm thấy dữ liệu xe vào ở cả Cache lẫn DB.", vehicleNo);
                return Map.of("message", "Không tìm thấy dữ liệu xe vào", "command", "DENY_BARRIER");
            }
            session = sessionOpt.get();
            log.info("[🔄 CACHE MISS] Tìm thấy xe {} dưới DB (Đi dài ngày hoặc mất cache). SessionId: {}", vehicleNo, session.getId());
        }

        int gracePeriod = systemConfigService.getGracePeriodMinutes();

        if (session.getBookingDetailId() == null) {

            boolean needsCalculation = session.getGracePeriodEnd() == null
                    || now.isAfter(session.getGracePeriodEnd());
            BigDecimal amountLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;

            if (needsCalculation) {
                billingService.calculateSessionFee(session, now);
                amountLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;

                if (amountLeft.compareTo(BigDecimal.ZERO) > 0) {
                    LocalDateTime paymentGracePeriod = now.plusMinutes(gracePeriod);
                    session.setGracePeriodEnd(paymentGracePeriod);
                    session.setAmountLeft(amountLeft);

                    if (isFromCache) {
                        cachedSession.setAmountLeft(amountLeft);
                        cachedSession.setAmountDue(session.getAmountDue());
                        cachedSession.setGracePeriodEnd(paymentGracePeriod.toString());

                        redisTemplate.opsForValue().set(redisKey, cachedSession, Duration.ofHours(24));
                        kafkaSessionProducerService.sendUpdateEvent(vehicleNo, cachedSession);
                    } else {
                        sessionRepo.save(session);
                        ParkingSessionEvent syncedCache = syncSessionToCache(session);

                    }
                    return Map.of(
                            "message", "Vui lòng thanh toán phí gửi xe",
                            "command", "DENY_BARRIER",
                            "session", session);
                }
            } else {
                if (amountLeft.compareTo(BigDecimal.ZERO) > 0) {
                    LocalDateTime safeBuffer = now.plusMinutes(gracePeriod);

                    if (session.getGracePeriodEnd().isBefore(safeBuffer)) {
                        log.info("⏳ Bơm thêm  ân hạn cho xe {} để bảo vệ kịp thu tiền mặt.", vehicleNo);
                        session.setGracePeriodEnd(safeBuffer);

                        if (isFromCache) {
                            cachedSession.setGracePeriodEnd(safeBuffer.toString());
                            redisTemplate.opsForValue().set(redisKey, cachedSession, Duration.ofHours(24));
                            kafkaSessionProducerService.sendUpdateEvent(vehicleNo, cachedSession);
                        } else {
                            ParkingSessionEvent syncedCache = syncSessionToCache(session);

                            sessionRepo.save(session);
                        }
                    }

                    redisTemplate.opsForValue().decrement("parking:system:total_current_occupancy", 1);
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Exit processed successfully");
                    response.put("session", session);
                    response.put("command", "DENY_BARRIER");
                    return response;
                }
            }
        }

        String safePlate = vehicleNo.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        long timestamp = java.time.Instant.now().toEpochMilli();
        String assignedImageName = "checkout_" + safePlate + "_" + timestamp + ".jpg";
        String imgUrl = "";
        if (isFromCache) {
            cachedSession.setExitTime(now.toString());
            cachedSession.setZoneOutId(request.getZoneId());
            cachedSession.setImageOutUrl(assignedImageName);
            cachedSession.setAmountLeft(BigDecimal.ZERO);
            cachedSession.setZoneOutId(request.getZoneId());
            imgUrl = cachedSession.getImageInUrl();
            redisTemplate.opsForValue().set(redisKey, cachedSession, Duration.ofMinutes(5));

            kafkaSessionProducerService.sendExitEvent(vehicleNo, cachedSession);
        } else {
            session.setExitTime(now);
            if (request.getZoneId() != null) {
                session.setZoneOut(zoneRepository.getReferenceById(request.getZoneId()));
            }
            imgUrl = session.getImageInUrl();
            session.setImageOutUrl(assignedImageName);
            sessionRepo.save(session);

            redisTemplate.delete(redisKey);

        }

        return Map.of(
                "message", "Chúc thượng lộ bình an",
                "command", "OPEN_BARRIER",
                "session", session,
                "assignedImageName", assignedImageName);
    }

    private Integer guessVehicleTypeIdFromPlate(String rawPlate) {
        if (rawPlate == null || rawPlate.isBlank()) {
            return 2; // Default to Motorbike (ID 2) if plate format is unidentifiable
        }

        String cleanPlate = rawPlate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanPlate.matches("^\\d{2}[A-Z]\\d{4,5}$")) {
            return 1; // ID 1: Car
        }

        return 2; // ID 2: Motorbike
    }


}