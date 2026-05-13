package com.smartparking.operation.service;

import com.smartparking.operation.dto.ParkingFeeQuote;
import com.smartparking.operation.entity.DayType;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.payment.service.BillingCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingFeeService {

    private final ParkingSessionRepository sessionRepo;
    private final BillingCalculatorService billingCalculatorService;

    public ParkingFeeQuote calculateFee(Long sessionId) {
        if (sessionId == null) throw new IllegalArgumentException("sessionId is required");

        ParkingSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("session not found"));

        LocalDateTime entryTime = session.getEntryTime();
        LocalDateTime calcTime = session.getExitTime() != null ? session.getExitTime() : LocalDateTime.now();

        long durationMinutes = 0;
        if (entryTime != null && calcTime != null && !calcTime.isBefore(entryTime)) {
            durationMinutes = Duration.between(entryTime, calcTime).toMinutes();
        }

        boolean subscription = session.getBookingDetailId() != null;
        boolean paid = Boolean.TRUE.equals(session.getIsPaid());
        DayType dayType = resolveDayType(calcTime != null ? calcTime : LocalDateTime.now());

        BigDecimal amount = BigDecimal.ZERO;
        if (!subscription && !paid && entryTime != null) {
            amount = billingCalculatorService.calculateGuestFee(
                    entryTime, calcTime, session.getVehicleTypeId(), dayType);
        }

        return new ParkingFeeQuote(
                session.getId(),
                session.getVehicleNo(),
                session.getVehicleTypeId(),
                entryTime,
                calcTime,
                durationMinutes,
                amount,
                dayType,
                paid,
                subscription
        );
    }

    private DayType resolveDayType(LocalDateTime time) {
        DayOfWeek dow = time.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return DayType.WEEKEND;
        }
        return DayType.WEEKDAY;
    }
}
