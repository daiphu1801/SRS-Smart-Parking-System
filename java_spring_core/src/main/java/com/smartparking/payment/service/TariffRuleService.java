package com.smartparking.payment.service;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TariffRuleService {

    private final TariffRuleRepository tariffRuleRepo;

    public List<TariffRule> list(Integer vehicleTypeId, DayType dayType, Boolean isActive) {
        if (vehicleTypeId != null && dayType != null) {
            if (isActive != null) {
                return tariffRuleRepo.findByVehicleTypeIdAndDayTypeOrderByStartTimeAsc(vehicleTypeId, dayType)
                        .stream().filter(r -> isActive.equals(r.getIsActive())).toList();
            }
            return tariffRuleRepo.findByVehicleTypeIdAndDayTypeOrderByStartTimeAsc(vehicleTypeId, dayType);
        }
        if (isActive != null) {
            return tariffRuleRepo.findByIsActiveOrderByStartTimeAsc(isActive);
        }
        return tariffRuleRepo.findAll(Sort.by("vehicleTypeId").ascending()
                .and(Sort.by("dayType").ascending())
                .and(Sort.by("startTime").ascending()));
    }

    public TariffRule create(TariffRule input) {
        TariffRule rule = copyForCreate(input);
        validate(rule, null);
        return tariffRuleRepo.save(rule);
    }

    public Optional<TariffRule> update(Integer id, TariffRule input) {
        return tariffRuleRepo.findById(id).map(existing -> {
            applyUpdate(existing, input);
            validate(existing, id);
            return tariffRuleRepo.save(existing);
        });
    }

    public boolean delete(Integer id) {
        return tariffRuleRepo.findById(id).map(rule -> {
            rule.setIsActive(false);
            tariffRuleRepo.save(rule);
            return true;
        }).orElse(false);
    }

    private TariffRule copyForCreate(TariffRule input) {
        TariffRule rule = new TariffRule();
        rule.setVehicleTypeId(input.getVehicleTypeId());
        rule.setDayType(input.getDayType());
        rule.setStartTime(input.getStartTime());
        rule.setEndTime(input.getEndTime());
        rule.setBaseBlockMins(input.getBaseBlockMins());
        rule.setBasePrice(input.getBasePrice());
        rule.setNextBlockMins(input.getNextBlockMins());
        rule.setNextBlockPrice(input.getNextBlockPrice());
        rule.setMaxPricePerDay(input.getMaxPricePerDay());
        rule.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        return rule;
    }

    private void applyUpdate(TariffRule target, TariffRule input) {
        if (input.getVehicleTypeId() != null) target.setVehicleTypeId(input.getVehicleTypeId());
        if (input.getDayType() != null) target.setDayType(input.getDayType());
        if (input.getStartTime() != null) target.setStartTime(input.getStartTime());
        if (input.getEndTime() != null) target.setEndTime(input.getEndTime());
        if (input.getBaseBlockMins() != null) target.setBaseBlockMins(input.getBaseBlockMins());
        if (input.getBasePrice() != null) target.setBasePrice(input.getBasePrice());
        if (input.getNextBlockMins() != null) target.setNextBlockMins(input.getNextBlockMins());
        if (input.getNextBlockPrice() != null) target.setNextBlockPrice(input.getNextBlockPrice());
        if (input.getMaxPricePerDay() != null) target.setMaxPricePerDay(input.getMaxPricePerDay());
        if (input.getIsActive() != null) target.setIsActive(input.getIsActive());
    }

    private void validate(TariffRule rule, Integer excludeId) {
        if (rule.getVehicleTypeId() == null) throw new IllegalArgumentException("vehicleTypeId is required");
        if (rule.getDayType() == null) throw new IllegalArgumentException("dayType is required");

        LocalTime start = rule.getStartTime();
        LocalTime end = rule.getEndTime();
        if (start == null || end == null) throw new IllegalArgumentException("startTime/endTime is required");
        if (!start.isBefore(end)) throw new IllegalArgumentException("startTime must be before endTime");

        if (rule.getBaseBlockMins() == null || rule.getBaseBlockMins() <= 0) {
            throw new IllegalArgumentException("baseBlockMins must be > 0");
        }
        if (!isNonNegative(rule.getBasePrice())) {
            throw new IllegalArgumentException("basePrice must be >= 0");
        }
        if (rule.getNextBlockMins() == null || rule.getNextBlockMins() <= 0) {
            throw new IllegalArgumentException("nextBlockMins must be > 0");
        }
        if (!isNonNegative(rule.getNextBlockPrice())) {
            throw new IllegalArgumentException("nextBlockPrice must be >= 0");
        }
        if (rule.getMaxPricePerDay() != null && !isNonNegative(rule.getMaxPricePerDay())) {
            throw new IllegalArgumentException("maxPricePerDay must be >= 0");
        }

        boolean active = rule.getIsActive() == null || Boolean.TRUE.equals(rule.getIsActive());
        if (active && tariffRuleRepo.existsOverlappingRule(
                rule.getVehicleTypeId(), rule.getDayType(), rule.getStartTime(), rule.getEndTime(), excludeId)) {
            throw new IllegalArgumentException("overlapping tariff rule exists");
        }
    }

    private boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }
}
