package com.smartparking.payment.service;

import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.service.system.IoTService;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.shared.kafka.dto.ParkingSessionEvent;
import com.smartparking.shared.service.SettingsService;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.smartparking.payment.service.SystemPaymentService.generatePayCode;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuardPaymentService {
    private final ParkingSessionRepository parkingSessionRepository;
    private final PaymentRepository paymentRepository;
    private final BillingService billingService;
    private final SettingsService settingService;
    private final IoTService iotService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemConfigService systemConfigService;

    @Transactional
    public Map<String, Object> processCashPayment(String vehicleNo, BigDecimal cashAmount) {
        String redisKey = iotService.getRedisKey(vehicleNo);
        ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);

        ParkingSession session;
        if (cachedSession == null) {
            log.warn("[⚠️ CACHE MISS] Không thấy xe {} trong Redis. Tiến hành mò xuống Database...", vehicleNo);
            session = parkingSessionRepository.findFirstByVehicleNoAndExitTimeIsNullOrderByIdDesc(vehicleNo)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy dữ liệu xe " + vehicleNo + " đang đỗ trong bãi. Vui lòng kiểm tra lại biển số."));

            log.info("[🔄 RESTORE] Tìm thấy xe đi dài ngày {} dưới DB. Khôi phục lại Cache.", vehicleNo);
            cachedSession = iotService.syncSessionToCache(session);
        } else {


            Long sessionId = cachedSession.getId() != null ? Long.valueOf(cachedSession.getId().toString()) : null;

            if (sessionId == null) {
                LocalDateTime entryTime = LocalDateTime.parse(cachedSession.getEntryTime());

                Optional<ParkingSession> dbSession = parkingSessionRepository.findFirstByVehicleNoAndExitTimeIsNullOrderByIdDesc(vehicleNo);

                if (dbSession.isPresent()) {
                    session = dbSession.get();
                    log.info("[KAFKA KỊP THỜI] Đã tìm thấy SessionID {} trong DB cho xe {}", session.getId(), vehicleNo);
                } else {
                    log.warn("[⚡ CƯỚP CÒ] Kafka quá chậm! Tự tạo Session DB để lấy ID thanh toán cho xe {}", vehicleNo);
                    BigDecimal cachePaid = cachedSession.getAmountPaid() != null ? new BigDecimal(cachedSession.getAmountPaid().toString()) : BigDecimal.ZERO;
                    BigDecimal cacheLeft = cachedSession.getAmountLeft() != null ? new BigDecimal(cachedSession.getAmountLeft().toString()) : BigDecimal.ZERO;
                    BigDecimal cacheDue = cachedSession.getAmountDue() != null ? new BigDecimal(cachedSession.getAmountDue().toString()) : BigDecimal.ZERO;
                    LocalDateTime cacheGrace = cachedSession.getGracePeriodEnd() != null ? LocalDateTime.parse(cachedSession.getGracePeriodEnd()) : null;

                    session = ParkingSession.builder()
                            .vehicleNo(vehicleNo)
                            .entryTime(entryTime)
                            .vehicleTypeId(cachedSession.getVehicleTypeId() != null ? Integer.valueOf(cachedSession.getVehicleTypeId().toString()) : null)
                            .zoneInId(cachedSession.getZoneInId() != null ? Integer.valueOf(cachedSession.getZoneInId().toString()) : null)
                            .imageInUrl(cachedSession.getImageInUrl() != null ? cachedSession.getImageInUrl() : null)
                            .bookingDetailId(cachedSession.getBookingDetailId() != null ? Integer.valueOf(cachedSession.getBookingDetailId().toString()) : null)
                            .amountPaid(cachePaid)
                            .amountLeft(cacheLeft)
                            .amountDue(cacheDue)
                            .gracePeriodEnd(cacheGrace)
                            .flagManual(false)
                            .build();

                    session = parkingSessionRepository.save(session);

                    cachedSession.setId(session.getId());
                    redisTemplate.opsForValue().set(redisKey, cachedSession, Duration.ofHours(24));
                }
            } else {
                session = parkingSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new BusinessException("Session không tồn tại dưới DB dù Cache báo có ID."));
            }
        }
        LocalDateTime now = LocalDateTime.now();

        boolean isGracePeriodExpired = session.getGracePeriodEnd() == null || now.isAfter(session.getGracePeriodEnd());
        if (isGracePeriodExpired) {
            throw new BusinessException("Cuốc xe đã quá thời gian ân hạn báo giá! Yêu cầu xe lùi lại để camera quét và cập nhật lại biểu phí mới.");
        }

        BigDecimal currentLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;

        if (currentLeft.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of("success", false, "message", "Cuốc xe này không còn nợ tiền, không cần thanh toán thêm!");
        }

        String payCode = generatePayCode();
        Payment payment = Payment.builder()
                .payCode(payCode)
                .method(PaymentMethod.CASH)
                .amount(cashAmount)
                .gateway("GUARD_DESK")
                .status(Status.SUCCESS)
                .parkingSessionId(session.getId())
                .build();
        paymentRepository.save(payment);

        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal updatedPaid = currentPaid.add(cashAmount);
        session.setAmountPaid(updatedPaid);

        BigDecimal newLeft = session.getAmountDue().subtract(updatedPaid);
        if (newLeft.compareTo(BigDecimal.ZERO) < 0) {
            newLeft = BigDecimal.ZERO;
        }
        session.setAmountLeft(newLeft);

        if (newLeft.compareTo(BigDecimal.ZERO) <= 0) {
            int gracePeriod = systemConfigService.getGracePeriodMinutes();

            session.setGracePeriodEnd(now.plusMinutes(gracePeriod));
            parkingSessionRepository.save(session);
            iotService.syncSessionToCache(session);

            return Map.of(
                    "success", true,
                    "message", "Thanh toán thành công. Hệ thống đã cấp thời gian ân hạn.",
                    "amountLeft", BigDecimal.ZERO
            );
        }

        parkingSessionRepository.save(session);
        iotService.syncSessionToCache(session);
        return Map.of(
                "success", true,
                "message", "Đã thu một phần. Xe vẫn còn nợ: " + newLeft,
                "amountLeft", newLeft
        );
    }

    @Transactional
    public Map<String, Object> processManualBankTransfer(String vehicleNo, BigDecimal newlyPaidAmount, String note) {
        // 1. TÌM CUỐC XE ĐANG ĐỖ (Ưu tiên xe chưa ra khỏi bãi)
        // =================================================================
        ParkingSession session = parkingSessionRepository.findFirstByVehicleNoAndExitTimeIsNullOrderByIdDesc(vehicleNo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe " + vehicleNo + " đang đỗ trong bãi."));

        // Lấy dữ liệu từ Cache (nếu có) để đảm bảo đồng bộ gốc tiền mới nhất
        String redisKey = iotService.getRedisKey(vehicleNo);
        ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);

        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;

        if (cachedSession != null && cachedSession.getAmountPaid() != null) {
            currentPaid = new BigDecimal(cachedSession.getAmountPaid().toString());
        }


        List<Status> successStatuses = List.of(Status.SUCCESS, Status.MANUAL_SUCCESS);
        boolean hasValidIntent = paymentRepository.existsByParkingSessionIdAndAmountAndStatusNotIn(
                session.getId(), newlyPaidAmount, successStatuses
        );

        if (!hasValidIntent) {
            throw new RuntimeException("Cảnh báo: Không tìm thấy giao dịch nào đang chờ khớp số tiền "
                    + newlyPaidAmount + " VNĐ cho xe này. Không thể xác nhận thanh toán khống!");
        }
        String payCode = generatePayCode();

        Payment payment = Payment.builder()
                .payCode(payCode)
                .method(PaymentMethod.BANK_TRANSFER)
                .amount(newlyPaidAmount)
                .gateway("GUARD_DESK")
                .gatewayResponse(note != null ? Map.of("guard_note", note) : null)
                .status(Status.MANUAL_SUCCESS)
                .parkingSessionId(session.getId())
                .transactionId( "Xác nhận bởi bảo vệ")
                .build();


        paymentRepository.save(payment);

        BigDecimal updatedPaid = currentPaid.add(newlyPaidAmount);
        session.setAmountPaid(updatedPaid);

        session.setAmountLeft(BigDecimal.ZERO);
        int gracePeriod = systemConfigService.getGracePeriodMinutes();

        session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(gracePeriod));

        parkingSessionRepository.save(session);
        iotService.syncSessionToCache(session);

        log.info("🛡️ GUARD ACTION: Bảo vệ xác nhận CK {} VNĐ cho xe {}. Đã xóa nợ và gia hạn mở cổng.", newlyPaidAmount, vehicleNo);

        return Map.of(
                "success", true,
                "message", "Xác nhận chuyển khoản thành công, đã mở cổng cho xe " + vehicleNo,
                "paidAmount", newlyPaidAmount
        );
    }

}