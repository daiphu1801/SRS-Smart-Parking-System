package com.smartparking.operation.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ParkingSessionResponse {
    private Long id;
    private Integer bookingDetailId;
    private Integer customerId; // Join từ BookingDetailId
    private String customerPhone;// Join từ BookingDetailId
    private String customerName;// Join từ BookingDetailId
    private String vehicleNo;
    private Integer vehicleTypeId; // Sau này có thể join lấy vehicleTypeName
    private String vehicleName; // Sau này có thể join lấy vehicleTypeName

    // Thông tin Vào
    private Integer zoneInId;      // Sau này có thể join lấy zoneInName
    private String zoneInName;
    private LocalDateTime entryTime;
    private String imageInUrl;

    // Thông tin Ra
    private Integer zoneOutId;
    private String zoneOutName;
    private LocalDateTime exitTime;
    private String imageOutUrl;

    // Thanh toán & Trạng thái
    private LocalDateTime gracePeriodEnd;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal amountLeft;
    private Boolean flagManual; // Cờ đánh dấu có sự can thiệp bằng tay của Bảo vệ
}