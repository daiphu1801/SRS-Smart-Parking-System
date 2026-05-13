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
        private String packagePriceName; // Ví dụ: "Gói 1 tháng xe máy"
        private BigDecimal price;        // Ví dụ: 150.000
        private Integer durationMonths;  // Ví dụ: 1
    }

    // (Bổ sung cho tư duy của ông) DTO dùng nội bộ trong Service để chứa kết quả đếm Quota
    @Data
    @Builder
    public static class GroupQuotaStatus {
        private Integer pkgVehTypeId;
        private Integer vehicleTypeId;
        private Integer currentUsage;
        private Integer maxQuantity;
        private boolean isAvailable; // = currentUsage < maxQuantity
    }
}
