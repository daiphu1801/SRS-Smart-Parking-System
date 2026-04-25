package com.smartparking.repository;

import com.smartparking.entity.DayType;
import com.smartparking.entity.TariffRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffRuleRepository extends JpaRepository<TariffRule, Integer> {
    
    List<TariffRule> findByVehicleTypeIdAndDayTypeAndIsActiveTrueOrderByStartTimeAsc(
        Integer vehicleTypeId, DayType dayType);
}
