package com.smartparking.operation.dto;

import com.smartparking.operation.entity.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BookingDetailDto {
    private Integer id;
    private Integer bookingId;
    private Integer customerId;
    private String customerPhone;
    private String customerName;
    private Integer packagePriceId;
    private BigDecimal price;
    private String packagePriceName;
    private Integer durationMonths;
    private String vehicleNo;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
