package com.smartparking.operation.specification;

import com.smartparking.operation.entity.ParkingSession;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingSessionSpecs {

    public static Specification<ParkingSession> hasVehicleNo(String vehicleNo) {
        return (root, query, cb) -> StringUtils.hasText(vehicleNo)
                ? cb.like(root.get("vehicleNo"), "%" + vehicleNo + "%")
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasFlagManual(Boolean flagManual) {
        return (root, query, cb) -> flagManual != null
                ? cb.equal(root.get("flagManual"), flagManual)
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasEntryTimeBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("entryTime"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("entryTime"), from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(root.get("entryTime"), to);
            }
            return cb.conjunction();
        };
    }

    public static Specification<ParkingSession> hasExitTimeBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("exitTime"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("exitTime"), from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(root.get("exitTime"), to);
            }
            return cb.conjunction();
        };
    }

    public static Specification<ParkingSession> hasBookingDetailId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("bookingDetailId"), id)
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasVehicleTypeId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("vehicleTypeId"), id)
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasZoneInId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("zoneInId"), id)
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasZoneOutId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("zoneOutId"), id)
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasAmountPaidGreaterThan(BigDecimal amount) {
        return (root, query, cb) -> amount != null
                ? cb.greaterThan(root.get("amountPaid"), amount)
                : cb.conjunction();
    }

    public static Specification<ParkingSession> hasAmountPaidLessThan(BigDecimal amount) {
        return (root, query, cb) -> amount != null
                ? cb.lessThan(root.get("amountPaid"), amount)
                : cb.conjunction();
    }
}
