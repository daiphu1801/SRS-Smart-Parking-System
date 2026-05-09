package com.smartparking.payment.service;

import com.smartparking.entity.DayType;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
import com.smartparking.subscription.controller.PackagePrice;
import com.smartparking.subscription.service.PackagePriceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingCalculatorService {

    private final TariffRuleRepository tariffRuleRepo;
    private final PackagePriceRepository packagePriceRepo;

    /**
     * Tính phí cho khách vãng lai (Guest) theo TariffRules (block time).
     */
    public BigDecimal calculateGuestFee(LocalDateTime entry, LocalDateTime exit, Integer vehicleTypeId,
            DayType dayType) {
        if (entry == null || exit == null || exit.isBefore(entry)) {
            return BigDecimal.ZERO;
        }

        List<TariffRule> rules = tariffRuleRepo
                .findByVehicleTypeIdAndDayTypeAndIsActiveTrueOrderByStartTimeAsc(vehicleTypeId, dayType);
        if (rules.isEmpty()) {
            log.warn("No tariff rules found for vehicleTypeId={} and dayType={}", vehicleTypeId, dayType);
            return BigDecimal.ZERO; // Default fallback
        }

        // Tính tổng số phút đỗ xe
        long durationMinutes = Duration.between(entry, exit).toMinutes();

        // Match rule cho thời điểm vào (entry time)
        LocalTime entryTime = entry.toLocalTime();
        TariffRule matchedRule = rules.stream()
                .filter(r -> !entryTime.isBefore(r.getStartTime()) && entryTime.isBefore(r.getEndTime()))
                .findFirst()
                .orElse(rules.get(0)); // Lấy rule đầu tiên nếu ko match

        BigDecimal totalFee = BigDecimal.ZERO;

        if (durationMinutes <= matchedRule.getBaseBlockMins()) {
            totalFee = matchedRule.getBasePrice();
        } else {
            totalFee = matchedRule.getBasePrice();
            long remainingMinutes = durationMinutes - matchedRule.getBaseBlockMins();

            if (matchedRule.getNextBlockMins() > 0) {
                long nextBlocks = (long) Math.ceil((double) remainingMinutes / matchedRule.getNextBlockMins());
                totalFee = totalFee.add(
                        matchedRule.getNextBlockPrice().multiply(BigDecimal.valueOf(nextBlocks)));
            }
        }

        // Kiểm tra max price per day nếu có cấu hình
        if (matchedRule.getMaxPricePerDay() != null && totalFee.compareTo(matchedRule.getMaxPricePerDay()) > 0) {
            totalFee = matchedRule.getMaxPricePerDay();
        }

        return totalFee;
    }

    /**
     * Tính tiền lẻ đăng ký gói dở dang trong tháng (pro-rated).
     */
    public BigDecimal calculateProRated(Integer packagePriceId, LocalDateTime startDate) {
        PackagePrice pp = packagePriceRepo.findById(packagePriceId).orElse(null);
        if (pp == null || pp.getPrice() == null) {
            return BigDecimal.ZERO;
        }

        int daysInMonth = startDate.toLocalDate().lengthOfMonth();
        int remainingDays = daysInMonth - startDate.getDayOfMonth() + 1;

        BigDecimal dailyRate = pp.getPrice().divide(BigDecimal.valueOf(daysInMonth), 2, java.math.RoundingMode.HALF_UP);
        return dailyRate.multiply(BigDecimal.valueOf(remainingDays));
    }
}
