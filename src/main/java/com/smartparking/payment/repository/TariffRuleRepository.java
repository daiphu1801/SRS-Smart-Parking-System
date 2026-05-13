package com.smartparking.payment.repository;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.entity.TariffRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

public interface TariffRuleRepository extends JpaRepository<TariffRule, Integer>,
		JpaSpecificationExecutor<TariffRule> {


	List<TariffRule> findByVehicleTypeIdAndIsActiveTrue(Integer vehicleTypeId);
	List<TariffRule> findByVehicleTypeIdAndDayTypeAndIsActiveTrue(Integer vehicleTypeId, DayType dayType);
}
