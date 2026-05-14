package com.smartparking.payment.service;

import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
import com.smartparking.shared.config.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final TariffRuleRepository tariffRuleRepository;
    private final SettingsService settingService; // Nếu ông có dùng

    // 1. BÊ HÀM NÀY TỪ IOT SERVICE SANG ĐÂY
    public  void calculateSessionFee(ParkingSession session, LocalDateTime now) {
        if (session.getBookingDetailId() != null) {
            return; // Xe thuê bao không mất phí block
        }
        List<TariffRule> rules = tariffRuleRepository.findByVehicleTypeIdAndIsActiveTrue(session.getVehicleType().getId());

        // 1. Gọi hàm tính TỔNG TIỀN từ lúc vào đến bây giờ
        BigDecimal totalDue = calculateTotalFeeByTimeSlicing(session.getEntryTime(), now, rules);

        // 2. Cập nhật lại số liệu sòng phẳng
        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newLeft = totalDue.subtract(currentPaid);

        // Bẫy số âm
        if (newLeft.compareTo(BigDecimal.ZERO) < 0) {
            newLeft = BigDecimal.ZERO;
        }

        session.setAmountDue(totalDue);
        session.setAmountLeft(newLeft);

    }

    private BigDecimal calculateTotalFeeByTimeSlicing(LocalDateTime entryTime, LocalDateTime exitTime, List<TariffRule> rules) {
        BigDecimal totalFee = BigDecimal.ZERO;
        LocalDateTime current = entryTime;

        while (current.isBefore(exitTime)) {
            // 1. Lấy thẳng tên ngày hiện tại từ Java (sẽ ra đúng MONDAY, TUESDAY,...)
            final LocalDateTime timeForLambda = current;
            String currentDayName = current.getDayOfWeek().name();
            LocalTime currentTime = current.toLocalTime();

            // 2. Tìm Rule khớp chính xác với Thứ và Khung giờ
            TariffRule activeRule = rules.stream()
                    .filter(r -> r.getDayType().name().equals(currentDayName)) // So khớp tên thứ
                    .filter(r -> isTimeInRange(currentTime, r.getStartTime(), r.getEndTime()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng giá TariffRule cho thời gian: " + timeForLambda));

            // 3. Tìm mốc kết thúc của "khúc" này (Chạm vạch hết Rule, hoặc chạm vạch Exit)
            LocalDateTime ruleEndTime;
            if (activeRule.getEndTime().equals(LocalTime.MIDNIGHT) || activeRule.getEndTime().equals(LocalTime.of(0, 0))) {
                // Nếu giờ kết thúc là 00:00 -> Hiểu là 24:00 (Sang ngày mới)
                ruleEndTime = current.toLocalDate().plusDays(1).atStartOfDay();
            } else {
                ruleEndTime = LocalDateTime.of(current.toLocalDate(), activeRule.getEndTime());
            }

            // Chọn mốc đến sớm hơn: Giờ kết thúc Rule hay Giờ xe ra?
            LocalDateTime chunkEnd = exitTime.isBefore(ruleEndTime) ? exitTime : ruleEndTime;

            // 4. Tính toán số phút và quy đổi ra tiền cho khúc này
            long minutesInChunk = Duration.between(current, chunkEnd).toMinutes();

            // Tính tiền tỉ lệ thuận theo phút (Vd: Giá 30k/h -> 30 phút là 15k)
            BigDecimal chunkFee = activeRule.getBasePrice()
                    .multiply(BigDecimal.valueOf(minutesInChunk))
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            totalFee = totalFee.add(chunkFee);

            // 5. Nhích con trỏ thời gian lên để tiếp tục vòng lặp
            current = chunkEnd;
        }

        return totalFee;
    }

    // Hàm bổ trợ giữ nguyên
    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        if (end.equals(LocalTime.MIDNIGHT) || end.equals(LocalTime.of(0,0))) {
            return !time.isBefore(start);
        }
        return !time.isBefore(start) && time.isBefore(end);
    }
}