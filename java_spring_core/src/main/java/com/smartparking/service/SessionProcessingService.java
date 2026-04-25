package com.smartparking.service;

import com.smartparking.config.SettingsService;
import com.smartparking.entity.BookingDetail;
import com.smartparking.entity.ParkingSession;
import com.smartparking.kafka.BarrierCommandProducer;
import com.smartparking.kafka.dto.BarrierCommand;
import com.smartparking.kafka.dto.VehicleEvent;
import com.smartparking.repository.BookingDetailRepository;
import com.smartparking.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionProcessingService {

    private final ParkingSessionRepository sessionRepo;
    private final BookingDetailRepository bookingDetailRepo;
    private final NavigationService navigationService;
    private final BarrierCommandProducer barrierProducer;
    private final SettingsService settings;

    @Transactional
    public void processVehicleEntry(VehicleEvent event) {
        log.info("ENTRY event: plate={} zone={}", event.getPlate(), event.getZoneId());
        Integer zoneId = event.getZoneId();

        if (isZoneFull(zoneId)) {
            barrierProducer.sendCommand(BarrierCommand.deny(event.getDeviceId(), null, "FULL"));
            log.warn("ZONE {} is full — deny entry for plate {}", zoneId, event.getPlate());
            return;
        }

        Optional<BookingDetail> subscription = bookingDetailRepo.findActiveByVehicleNo(
            event.getPlate(), LocalDateTime.now());

        ParkingSession session = ParkingSession.builder()
            .vehicleNo(event.getPlate())
            .vehicleTypeId(event.getVehicleTypeId())
            .zoneInId(zoneId)
            .entryTime(LocalDateTime.now())
            .imageInUrl(event.getImageUrl())
            .bookingDetailId(subscription.map(BookingDetail::getId).orElse(null))
            .build();

        sessionRepo.save(session);
        navigationService.incrementOccupancy(zoneId);
        barrierProducer.sendCommand(BarrierCommand.open(event.getDeviceId(), session.getId(), "OK"));
        log.info("OPEN barrier plate={} sessionId={} type={}",
            event.getPlate(), session.getId(), subscription.isPresent() ? "SUBSCRIBER" : "GUEST");
    }

    @Transactional
    public void processVehicleExit(VehicleEvent event) {
        log.info("EXIT event: plate={} zone={}", event.getPlate(), event.getZoneId());

        ParkingSession session = sessionRepo.findOpenSession(event.getPlate()).orElse(null);

        if (session == null) {
            barrierProducer.sendCommand(BarrierCommand.open(event.getDeviceId(), null, "NO_SESSION"));
            log.warn("No open session for plate {} — forcing open", event.getPlate());
            return;
        }

        if (session.getBookingDetailId() != null) {
            closeSession(session, event);
            barrierProducer.sendCommand(BarrierCommand.open(event.getDeviceId(), session.getId(), "SUBSCRIBER"));
            return;
        }

        if (Boolean.TRUE.equals(session.getIsPaid())
                && session.getGracePeriodEnd() != null
                && LocalDateTime.now().isBefore(session.getGracePeriodEnd())) {
            closeSession(session, event);
            barrierProducer.sendCommand(BarrierCommand.open(event.getDeviceId(), session.getId(), "PAID_IN_GRACE"));
            return;
        }

        barrierProducer.sendCommand(BarrierCommand.deny(event.getDeviceId(), session.getId(), "PAYMENT_REQUIRED"));
        log.warn("DENY exit plate={} — payment required", event.getPlate());
    }

    private void closeSession(ParkingSession session, VehicleEvent event) {
        session.setExitTime(LocalDateTime.now());
        session.setZoneOutId(event.getZoneId());
        session.setImageOutUrl(event.getImageUrl());
        sessionRepo.save(session);
        navigationService.decrementOccupancy(event.getZoneId());
    }

    private boolean isZoneFull(Integer zoneId) {
        return navigationService.isAtCapacity(zoneId, settings.getSafetyBufferPercent());
    }
}
