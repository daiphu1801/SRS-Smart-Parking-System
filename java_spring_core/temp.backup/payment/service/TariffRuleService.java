package com.smartparking.payment.service;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.dto.request.TariffRuleCreateRequest;
import com.smartparking.payment.dto.response.TariffRuleResponse;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.TariffRuleRepository;
import com.smartparking.payment.specification.TariffRuleSpecs;
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

    // 1. Get List (Có phân trang và Lọc)
    public Page<TariffRuleResponse> getTariffRules(Integer vehicleTypeId, DayType dayType, Boolean isActive, Pageable pageable) {
        Specification<TariffRule> spec = Specification
                .where(TariffRuleSpecs.hasVehicleTypeId(vehicleTypeId))
                .and(TariffRuleSpecs.hasDayType(dayType))
                .and(TariffRuleSpecs.hasIsActive(isActive));

        Page<TariffRule> rules = tariffRuleRepository.findAll(spec, pageable);
        return rules.map(this::mapToResponse); // Dùng method reference map data cực gọn
    }

    // 2. Get Detail
    public TariffRuleResponse getTariffRuleById(Integer id) {
        TariffRule rule = getRuleOrThrow(id);
        return mapToResponse(rule);
    }

    // 3. Create mới bảng giá
    @Transactional
    public TariffRuleResponse createTariffRule(TariffRuleCreateRequest request) {
        validateTimeSequence(request.getStartTime(), request.getEndTime());
        boolean isRuleActive = request.getIsActive() != null ? request.getIsActive() : true;

        if (isRuleActive) {
            validateTimeOverlap(
                    null, // Đang tạo mới nên không có ID
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
                .isActive(request.getIsActive() != null ? request.getIsActive() : true) // Mặc định là true nếu FE không gửi
                .build();

        return mapToResponse(tariffRuleRepository.save(newRule));
    }

    // 4. Update bảng giá hiện tại
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

        // Hibernate Dirty Checking sẽ tự động update xuống DB khi kết thúc Transaction
        return mapToResponse(tariffRuleRepository.save(existingRule));
    }

    // 5. Disable bảng giá (Soft Delete)
    @Transactional
    public void disableTariffRule(Integer id) {
        TariffRule existingRule = getRuleOrThrow(id);
        existingRule.setIsActive(false);
        tariffRuleRepository.save(existingRule);
    }

    // --- CÁC HÀM HELPER ---

    // Hàm dùng chung để bắt lỗi 404 cho chuẩn
    private TariffRule getRuleOrThrow(Integer id) {
        return tariffRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng giá với ID: " + id));
    }

    // Hàm chuyển Entity thành DTO Response
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
        // Lấy tất cả các rule đang active của cùng loại xe và loại ngày
        List<TariffRule> existingRules = tariffRuleRepository.findByVehicleTypeIdAndDayTypeAndIsActiveTrue(vehicleTypeId, dayType);

        for (TariffRule existing : existingRules) {
            // Bỏ qua chính bản ghi đang được update
            if (currentRuleId != null && currentRuleId.equals(existing.getId())) {
                continue;
            }

            // Kiểm tra trùng lặp
            if (isOverlap(newStart, newEnd, existing.getStartTime(), existing.getEndTime())) {
                throw new RuntimeException(String.format(
                        "Thời gian cấu hình (%s - %s) bị trùng lặp với bảng giá ID %d (%s - %s)!",
                        newStart, newEnd, existing.getId(), existing.getStartTime(), existing.getEndTime()
                ));
            }
        }
    }

    private void validateTimeSequence(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new RuntimeException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc! Nếu ca làm việc vắt qua đêm, vui lòng tách thành 2 bảng giá (VD: Thứ Sáu 22:00-23:59 và Thứ Bảy 00:00-06:00).");
        }
    }

    private boolean isOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
         return start1.isBefore(end2) && start2.isBefore(end1);
    }


}