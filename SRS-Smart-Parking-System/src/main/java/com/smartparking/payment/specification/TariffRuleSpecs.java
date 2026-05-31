package com.smartparking.payment.specification;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.entity.TariffRule;
import org.springframework.data.jpa.domain.Specification;

public class TariffRuleSpecs {

    public static Specification<TariffRule> hasVehicleTypeId(Integer vehicleTypeId) {
        return (root, query, cb) -> vehicleTypeId != null
                ? cb.equal(root.get("vehicleTypeId"), vehicleTypeId)
                : cb.conjunction();
    }

    public static Specification<TariffRule> hasDayType(DayType dayType) {
        return (root, query, cb) -> dayType != null
                ? cb.equal(root.get("dayType"), dayType)
                : cb.conjunction();
    }

    public static Specification<TariffRule> hasIsActive(Boolean isActive) {
        return (root, query, cb) -> isActive != null
                ? cb.equal(root.get("isActive"), isActive)
                : cb.conjunction();
    }
}