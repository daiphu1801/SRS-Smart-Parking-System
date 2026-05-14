package com.smartparking.operation.specification;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDetailSpecs {
    public static Specification<BookingDetail> hasVehicleNoIn(List<String> vehicleNos) {
        return (root, query, cb) -> {
            if (vehicleNos == null || vehicleNos.isEmpty()) return cb.conjunction();
            return root.get("vehicleNo").in(vehicleNos);
        };
    }

    public static Specification<BookingDetail> hasStatus(BookingStatus status) {
        return (root, query, cb) -> status != null
                ? cb.equal(root.get("status"), status)
                : cb.conjunction();
    }
    public static Specification<BookingDetail> hasStatusIn(List<BookingStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }
}
