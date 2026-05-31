package com.smartparking.operation.service.admin;

import com.smartparking.operation.dto.request.ManualSessionUpdateRequest;
import com.smartparking.operation.dto.request.ParkingSessionFilterRequest;
import com.smartparking.operation.dto.response.ParkingSessionResponse;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.specification.ParkingSessionSpecs;
import com.smartparking.shared.dto.PageResponse;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.subscription.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    @Transactional(readOnly = true)
    public PageResponse<ParkingSessionResponse> getAllSessions(ParkingSessionFilterRequest filter, Pageable pageable) {

        Specification<ParkingSession> spec = Specification
                .where(ParkingSessionSpecs.hasVehicleNo(filter.getVehicleNo()))
                .and(ParkingSessionSpecs.hasFlagManual(filter.getFlagManual()))
                .and(ParkingSessionSpecs.isCurrentlyParked(filter.getIsCurrentlyParked()))
                .and(ParkingSessionSpecs.hasEntryTimeBetween(filter.getEntryTimeFrom(), filter.getEntryTimeTo()))
                .and(ParkingSessionSpecs.hasExitTimeBetween(filter.getExitTimeFrom(), filter.getExitTimeTo()))
                .and(ParkingSessionSpecs.hasBookingDetailId(filter.getBookingDetailId()))
                .and(ParkingSessionSpecs.hasVehicleTypeId(filter.getVehicleTypeId()))
                .and(ParkingSessionSpecs.hasZoneInId(filter.getZoneInId()))
                .and(ParkingSessionSpecs.hasZoneOutId(filter.getZoneOutId()))
                .and(ParkingSessionSpecs.hasAmountPaidGreaterThan(filter.getPaidGreaterThan()))
                .and(ParkingSessionSpecs.hasAmountPaidLessThan(filter.getPaidLessThan()));

        Page<ParkingSession> sessionPage = parkingSessionRepository.findAll(spec, pageable);

        List<ParkingSessionResponse> content = sessionPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, sessionPage.getTotalElements(), sessionPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ParkingSessionResponse getSessionDetail(Long id) {
        ParkingSession session = parkingSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Lỗi: Không tìm thấy phiên đỗ xe với ID " + id));
        return mapToResponse(session);
    }

    @Transactional
    public ParkingSessionResponse updateSessionManually(Long id, ManualSessionUpdateRequest request) {
        ParkingSession session = parkingSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Lỗi: Không tìm thấy phiên đỗ xe với ID " + id));

        if (request.getCorrectVehicleNo() != null) {
            session.setVehicleNo(request.getCorrectVehicleNo());
        }
        if (request.getUpdateVehicleTypeId() != null) {
            session.setVehicleType(vehicleTypeRepository.getReferenceById(request.getUpdateVehicleTypeId()));
        }
        if (request.getUpdateAmountPaid() != null) {
            session.setAmountPaid(request.getUpdateAmountPaid());
        }

        // Recalculate amount left
        BigDecimal due = session.getAmountDue() != null ? session.getAmountDue() : BigDecimal.ZERO;
        BigDecimal paid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        session.setAmountLeft(due.subtract(paid));

        session.setFlagManual(true);

        session = parkingSessionRepository.save(session);
        return mapToResponse(session);
    }


    private ParkingSessionResponse mapToResponse(ParkingSession ps) {
        return ParkingSessionResponse.builder()
                .id(ps.getId())
                .bookingDetailId(ps.getBookingDetailId())
                .customerId(ps.getBookingDetail() != null ? ps.getBookingDetail().getCustomerId() : null)
                .vehicleNo(ps.getVehicleNo())
                .vehicleTypeId(ps.getVehicleTypeId())
                .vehicleName(ps.getVehicleType() != null ? ps.getVehicleType().getTypeName() : null)
                .zoneInId(ps.getZoneInId())
                .zoneInName(ps.getZoneIn() != null ? ps.getZoneIn().getZoneName() : null)
                .entryTime(ps.getEntryTime())
                .imageInUrl(ps.getImageInUrl())
                .zoneOutId(ps.getZoneOutId())
                .zoneOutName(ps.getZoneOut() != null ? ps.getZoneOut().getZoneName() : null)
                .exitTime(ps.getExitTime())
                .imageOutUrl(ps.getImageOutUrl())
                .gracePeriodEnd(ps.getGracePeriodEnd())
                .amountDue(ps.getAmountDue())
                .amountPaid(ps.getAmountPaid())
                .amountLeft(ps.getAmountLeft())
                .flagManual(ps.getFlagManual())
                .build();
    }
}
