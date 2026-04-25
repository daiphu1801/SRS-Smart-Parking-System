package com.smartparking.service;

import com.smartparking.entity.Zone;
import com.smartparking.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationService {

    private final ZoneRepository zoneRepo;

    @Transactional
    public void incrementOccupancy(Integer zoneId) { updateOccupancy(zoneId, +1); }

    @Transactional
    public void decrementOccupancy(Integer zoneId) { updateOccupancy(zoneId, -1); }

    private void updateOccupancy(Integer zoneId, int delta) {
        Zone zone = zoneRepo.findById(zoneId).orElse(null);
        if (zone == null) return;
        zone.setCurrentOccupancy(Math.max(0, zone.getCurrentOccupancy() + delta));
        zoneRepo.save(zone);
        if (zone.getParentZoneId() != null) updateOccupancy(zone.getParentZoneId(), delta);
    }

    public boolean isAtCapacity(Integer zoneId, int bufferPercent) {
        return zoneRepo.findById(zoneId).map(zone -> {
            if (zone.getCapacity() == null || zone.getCapacity() == 0) return false;
            int cutoff = (int) Math.ceil(zone.getCapacity() * (1.0 - bufferPercent / 100.0));
            return zone.getCurrentOccupancy() >= cutoff;
        }).orElse(false);
    }

    public List<Zone> getZoneTree() { return zoneRepo.findAll(); }
}
