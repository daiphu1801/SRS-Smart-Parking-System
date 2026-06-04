package com.smartparking.payment.service;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.dto.request.TariffRuleCreateRequest;
import com.smartparking.payment.dto.response.TariffRuleResponse;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
import com.smartparking.payment.specification.TariffRuleSpecs;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffRuleService {

    private final TariffRuleRepository tariffRuleRepository;

    /**
     * Retrieves a paginated list of tariff rules based on vehicle type, day type, and active status.
     *
     * @param vehicleTypeId The ID of the vehicle type (e.g., Car, Motorbike).
     * @param dayType       The type of day (e.g., WEEKDAY, WEEKEND).
     * @param isActive      Filter by active status.
     * @param pageable      Pagination and sorting information.
     * @return A paginated list of TariffRuleResponse.
     */
    public Page<TariffRuleResponse> getTariffRules(Integer vehicleTypeId, DayType dayType, Boolean isActive, Pageable pageable) {
        Specification<TariffRule> spec = Specification
                .where(TariffRuleSpecs.hasVehicleTypeId(vehicleTypeId))
                .and(TariffRuleSpecs.hasDayType(dayType))
                .and(TariffRuleSpecs.hasIsActive(isActive));

        Page<TariffRule> rules = tariffRuleRepository.findAll(spec, pageable);
        return rules.map(this::mapToResponse);
    }

    /**
     * Retrieves details of a specific tariff rule by its ID.
     *
     * @param id The ID of the tariff rule.
     * @return The TariffRuleResponse DTO.
     * @throws BusinessException If the rule is not found.
     */
    public TariffRuleResponse getTariffRuleById(Integer id) {
        TariffRule rule = getRuleOrThrow(id);
        return mapToResponse(rule);
    }

    /**
     * Creates a new tariff rule, validating time sequences and overlaps to ensure billing integrity.
     *
     * @param request The data for the new tariff rule.
     * @return The created TariffRuleResponse.
     * @throws BusinessException If time constraints are violated (e.g., start time after end time, or overlapping rules).
     */
    @Transactional
    public TariffRuleResponse createTariffRule(TariffRuleCreateRequest request) {
        validateTimeSequence(request.getStartTime(), request.getEndTime());
        boolean isRuleActive = request.getIsActive() != null ? request.getIsActive() : true;

        if (isRuleActive) {
            validateTimeOverlap(
                    null, // Pass null as ID for new records during overlap validation
                    request.getVehicleTypeId(),
                    request.getDayType(),
                    request.getStartTime(),
                    request.getEndTime()
            );
        }
        TariffRule newRule = TariffRule.builder()
                .vehicleTypeId(request.getVehicleTypeId())
                .dayType(request.getDayType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .basePrice(request.getBasePrice())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true) // Default to active if client does not specify
                .build();

        return mapToResponse(tariffRuleRepository.save(newRule));
    }

    /**
     * Updates an existing tariff rule. Re-validates time constraints against other existing rules.
     *
     * @param id      The ID of the rule to update.
     * @param request The updated data.
     * @return The updated TariffRuleResponse.
     */
    @Transactional
    public TariffRuleResponse updateTariffRule(Integer id, TariffRuleCreateRequest request) {
        validateTimeSequence(request.getStartTime(), request.getEndTime());
        TariffRule existingRule = getRuleOrThrow(id);
        boolean isRuleActive = request.getIsActive() != null ? request.getIsActive() : existingRule.getIsActive();

        if (isRuleActive) {
            validateTimeOverlap(
                    existingRule.getId(),
                    request.getVehicleTypeId(),
                    request.getDayType(),
                    request.getStartTime(),
                    request.getEndTime()
            );
        }

        existingRule.setVehicleTypeId(request.getVehicleTypeId());
        existingRule.setDayType(request.getDayType());
        existingRule.setStartTime(request.getStartTime());
        existingRule.setEndTime(request.getEndTime());
        existingRule.setBasePrice(request.getBasePrice());

        if (request.getIsActive() != null) {
            existingRule.setIsActive(request.getIsActive());
        }

        // Hibernate Dirty Checking automatically flushes changes to the database at transaction commit
        return mapToResponse(tariffRuleRepository.save(existingRule));
    }

    /**
     * Soft deletes a tariff rule by setting its status to inactive.
     *
     * @param id The ID of the rule to disable.
     */
    @Transactional
    public void disableTariffRule(Integer id) {
        TariffRule existingRule = getRuleOrThrow(id);
        existingRule.setIsActive(false);
        tariffRuleRepository.save(existingRule);
    }

    // --- Helper Methods ---

    // Standardized method to fetch an entity or throw a standard 404 BusinessException
    private TariffRule getRuleOrThrow(Integer id) {
        return tariffRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bảng giá với ID: " + id));
    }

    // Maps a TariffRule entity to a TariffRuleResponse DTO
    private TariffRuleResponse mapToResponse(TariffRule rule) {
        return TariffRuleResponse.builder()
                .id(rule.getId())
                .vehicleTypeId(rule.getVehicleTypeId())
                .dayType(rule.getDayType())
                .startTime(rule.getStartTime())
                .endTime(rule.getEndTime())
                .basePrice(rule.getBasePrice())
                .isActive(rule.getIsActive())
                .build();
    }


    private void validateTimeOverlap(Integer currentRuleId, Integer vehicleTypeId, DayType dayType, LocalTime newStart, LocalTime newEnd) {
        // Fetch all active rules for the same vehicle type and day type
        List<TariffRule> existingRules = tariffRuleRepository.findByVehicleTypeIdAndDayTypeAndIsActiveTrue(vehicleTypeId, dayType);

        for (TariffRule existing : existingRules) {
            // Skip the current rule being updated to prevent self-overlap detection
            if (currentRuleId != null && currentRuleId.equals(existing.getId())) {
                continue;
            }

            // Check for time range overlap
            if (isOverlap(newStart, newEnd, existing.getStartTime(), existing.getEndTime())) {
                throw new BusinessException(String.format(
                        "Thời gian cấu hình (%s - %s) bị trùng lặp với bảng giá ID %d (%s - %s)!",
                        newStart, newEnd, existing.getId(), existing.getStartTime(), existing.getEndTime()
                ));
            }
        }
    }

    private void validateTimeSequence(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc! Nếu ca làm việc vắt qua đêm, vui lòng tách thành 2 bảng giá (VD: Thứ Sáu 22:00-23:59 và Thứ Bảy 00:00-06:00).");
        }
    }

    private boolean isOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
         return start1.isBefore(end2) && start2.isBefore(end1);
    }


}