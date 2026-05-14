package com.smartparking.operation.service.system;

import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.IotEntryRequest;
import com.smartparking.operation.dto.request.IotExitRequest;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.entity.Zone;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.repository.ZoneRepository;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
import com.smartparking.payment.service.BillingService;
import com.smartparking.shared.config.SettingsService;
import com.smartparking.subscription.entity.VehicleType;
import com.smartparking.subscription.repository.VehicleTypeRepository;
import com.smartparking.operation.service.NavigationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    private final IoTZoneService iotZoneService ;
    private final BillingService billingService;


    @Transactional
    public Map<String, Object> handleEntry(IotEntryRequest request) {
        log.info("Processing entry from IoT: {}", request.getVehicleNo());

        Optional<ParkingSession> existingSession = sessionRepo.findOpenSession(request.getVehicleNo());
        if (existingSession.isPresent()) {
            log.warn("Xe {} đã ở trong bãi, bỏ qua yêu cầu check-in đúp.", request.getVehicleNo());
            return Map.of(
                    "message", "Xe đã ở trong bãi",
                    "command", "OPEN_BARRIER" // Vẫn mở cổng vì có thể lúc nãy cổng lỗi chưa mở
            );
        }


        LocalDateTime now = LocalDateTime.now();

        BookingDetail activeBooking = bookingDetailRepo
                .findFirstByVehicleNoAndStatus(request.getVehicleNo(), BookingStatus.ACTIVE)
                .orElse(null);

        Zone zoneIn = request.getZoneId() != null ? zoneRepository.getReferenceById(request.getZoneId()) : null;

        Integer typeId = request.getVehicleTypeId();
        if (typeId == null) {
            typeId = guessVehicleTypeIdFromPlate(request.getVehicleNo());
        }
        VehicleType vehicleType = vehicleTypeRepository.getReferenceById(typeId);


        ParkingSession session = ParkingSession.builder()
                .vehicleNo(request.getVehicleNo())
                .vehicleType(vehicleType)
                .zoneIn(zoneIn)
                .entryTime(now)
                .imageInUrl(request.getImageUrl())
                .bookingDetailId(activeBooking != null ? activeBooking.getId() : null)
                .build();

        session = sessionRepo.save(session);

         if (request.getZoneId() != null) {
             iotZoneService.updateZoneTransition(request.getDeviceId());
         }

        return Map.of(
                "message", "Entry processed successfully",
                "sessionId", session.getId(),
                "command", "OPEN_BARRIER",
                "Plate number",request.getVehicleNo() ,
                "type", activeBooking != null ? "SUBSCRIBER" : "GUEST");
    }

    @Transactional
    public Map<String, Object> handleExit(IotExitRequest request) {
        log.info("Processing exit from IoT: {}", request.getVehicleNo());

        Optional<ParkingSession> sessionOpt = sessionRepo.findOpenSession(request.getVehicleNo());
        if (sessionOpt.isEmpty()) {
            log.warn("Cảnh báo: Xe {} ra cổng nhưng không có phiên đỗ xe (Chưa check-in)", request.getVehicleNo());
            return Map.of(
                    "message", "Không tìm thấy dữ liệu xe vào",
                    "command", "DENY_BARRIER");
        }

        ParkingSession session = sessionOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Xử lý xe vãng lai (Không phải vé tháng)
        if (session.getBookingDetailId() == null) {
            BigDecimal amountLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;

            // KIỂM TRA XEM CÓ CẦN TÍNH LẠI TIỀN KHÔNG?
            // Điều kiện cần tính lại: Chưa có hạn ân hạn HOẶC Đã quá hạn ân hạn HOẶC Đang còn nợ tiền
            boolean needsCalculation = session.getGracePeriodEnd() == null
                    || now.isAfter(session.getGracePeriodEnd())
                    || amountLeft.compareTo(BigDecimal.ZERO) > 0;

            if (needsCalculation) {
                billingService.calculateSessionFee(session, now); // Chỉ tính tiền, KHÔNG cấp ân hạn ở đây

                // Lấy lại amountLeft sau khi tính toán
                amountLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;

                if (amountLeft.compareTo(BigDecimal.ZERO) > 0) {
                    sessionRepo.save(session);
                    return Map.of(
                            "message", "Vui lòng thanh toán phí gửi xe",
                            "sessionId", session.getId(),
                            "amountLeft", amountLeft,
                            "command", "DENY_BARRIER");
                }
            }
        }

        session.setExitTime(now);
        if (request.getZoneId() != null) {
            session.setZoneOut(zoneRepository.getReferenceById(request.getZoneId()));
        }
        session.setImageOutUrl(request.getImageUrl());

        sessionRepo.save(session);
        return Map.of(
                "message", "Chúc thượng lộ bình an",
                "sessionId", session.getId(),
                "command", "OPEN_BARRIER");
    }

    private Integer guessVehicleTypeIdFromPlate(String rawPlate) {
        if (rawPlate == null || rawPlate.isBlank()) {
            return 2; // Mặc định cho vào xe máy nếu không đọc được
        }

        String cleanPlate = rawPlate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanPlate.matches("^\\d{2}[A-Z]\\d{4,5}$")) {
            return 1; // ID 1: Ô tô
        }

        return 2; // ID 2: Xe máy
    }


}