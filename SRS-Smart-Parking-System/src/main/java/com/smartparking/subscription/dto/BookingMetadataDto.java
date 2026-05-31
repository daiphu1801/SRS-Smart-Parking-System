package com.smartparking.subscription.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

public class BookingMetadataDto {

    // 1. DTO Trả về cho API Lấy danh sách Loại xe khả dụng
    @Data
    @Builder
    public static class AllowedVehicleTypeResponse {
        private Integer vehicleTypeId;
        private String vehicleTypeName; // Ví dụ: "Ô tô", "Xe máy"
        private Integer currentQuantity; // Đã dùng (Ví dụ: 1)
        private Integer maxQuantity;     // Tối đa được phép (Ví dụ: 2)
        // Frontend sẽ dùng current và max để disable dropdown nếu current >= max
    }

    // 2. DTO Trả về cho API Lấy danh sách Gói cước sau khi chọn Loại xe
    @Data
    @Builder
    public static class AvailablePackagePriceResponse {
        private Integer packagePriceId;
        private String packagePriceName;
        private BigDecimal price;
        private Integer durationMonths;
    }
}
