package com.smartparking.payment.repository;

import com.smartparking.entity.DayType;
import com.smartparking.payment.entity.TariffRule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffRuleRepository extends JpaRepository<TariffRule, Integer> {

    List<TariffRule> findByVehicleTypeIdAndDayTypeAndIsActiveTrueOrderByStartTimeAsc(
            Integer vehicleTypeId, DayType dayType);
}
