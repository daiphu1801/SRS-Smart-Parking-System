package com.smartparking.payment.service;

import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
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

    public  void calculateSessionFee(ParkingSession session, LocalDateTime now) {
        if (session.getBookingDetailId() != null) {
            return;
        }
        List<TariffRule> rules = tariffRuleRepository.findByVehicleTypeIdAndIsActiveTrue(session.getVehicleTypeId());
        log.info("🚨 CHECK DB: Lấy được {} bảng giá cho VehicleTypeId = {}", rules.size(), session.getVehicleTypeId());

        BigDecimal rawTotalDue = calculateTotalFeeByTimeSlicing(session.getEntryTime(), now, rules,session.getVehicleNo());

        BigDecimal totalDue = rawTotalDue.setScale(0, RoundingMode.HALF_UP);
        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newLeft = totalDue.subtract(currentPaid);

        if (newLeft.compareTo(BigDecimal.ZERO) < 0) {
            newLeft = BigDecimal.ZERO;
        }

        session.setAmountDue(totalDue);
        session.setAmountLeft(newLeft);
        log.info("💰 CHỐT BILL - Xe {}: Tổng phí = {}, Đã trả = {}, Còn nợ = {}",
                session.getVehicleNo(), totalDue, currentPaid, newLeft);
    }

        private BigDecimal calculateTotalFeeByTimeSlicing(LocalDateTime entryTime, LocalDateTime exitTime, List<TariffRule> rules, String vehicleNo) {
            BigDecimal totalFee = BigDecimal.ZERO;
            LocalDateTime current = entryTime;

            while (current.isBefore(exitTime)) {
                final LocalDateTime timeForLambda = current;
                String currentDayName = current.getDayOfWeek().name();
                LocalTime currentTime = current.toLocalTime();

                TariffRule activeRule = rules.stream()
                        .filter(r -> r.getDayType().name().equals(currentDayName)) // So khớp tên thứ
                        .filter(r -> isTimeInRange(currentTime, r.getStartTime(), r.getEndTime()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                String.format("LỖI CẤU HÌNH GIÁ: Không tìm thấy TariffRule cho xe %s vào lúc %s (%s). Vui lòng báo Admin kiểm tra lại bảng giá!",
                                        vehicleNo, timeForLambda, currentDayName)
                        ));

                LocalDateTime ruleEndTime;

                if (activeRule.getEndTime().equals(LocalTime.of(23, 59, 59)) ||
                        activeRule.getEndTime().equals(LocalTime.MIDNIGHT) ||
                        activeRule.getEndTime().equals(LocalTime.of(0, 0))) {
                    ruleEndTime = current.toLocalDate().plusDays(1).atStartOfDay();
                } else {
                    ruleEndTime = LocalDateTime.of(current.toLocalDate(), activeRule.getEndTime());
                }

                LocalDateTime chunkEnd = exitTime.isBefore(ruleEndTime) ? exitTime : ruleEndTime;
                long minutesInChunk = Duration.between(current, chunkEnd).toMinutes();
                BigDecimal chunkFee = activeRule.getBasePrice()
                        .multiply(BigDecimal.valueOf(minutesInChunk))
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                totalFee = totalFee.add(chunkFee);

                current = chunkEnd;
            }

            return totalFee;
        }

    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        if (end.equals(LocalTime.of(23, 59, 59)) || end.equals(LocalTime.MIDNIGHT) || end.equals(LocalTime.of(0,0))) {
            return !time.isBefore(start);
        }
        return !time.isBefore(start) && time.isBefore(end);
    }
}