package com.smartparking.payment.repository;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.entity.TariffRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TariffRuleRepository extends JpaRepository<TariffRule, Integer> {
    List<TariffRule> findByVehicleTypeIdAndDayTypeAndIsActiveTrueOrderByStartTimeAsc(
	    Integer vehicleTypeId, DayType dayType);

    List<TariffRule> findByVehicleTypeIdAndDayTypeOrderByStartTimeAsc(
	    Integer vehicleTypeId, DayType dayType);

    List<TariffRule> findByIsActiveOrderByStartTimeAsc(Boolean isActive);

    @Query("""
	select (count(tr) > 0) from TariffRule tr
	where tr.vehicleTypeId = :vehicleTypeId
	  and tr.dayType = :dayType
	  and tr.isActive = true
	  and (:excludeId is null or tr.id <> :excludeId)
	  and (:startTime < tr.endTime and :endTime > tr.startTime)
    """)
    boolean existsOverlappingRule(
	    @Param("vehicleTypeId") Integer vehicleTypeId,
	    @Param("dayType") DayType dayType,
	    @Param("startTime") LocalTime startTime,
	    @Param("endTime") LocalTime endTime,
	    @Param("excludeId") Integer excludeId
    );
}
