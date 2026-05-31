package com.smartparking.operation.dto.response;

import com.smartparking.operation.dto.BookingDetailDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tổng hợp trả về cho endpoint GET /api/v1/customer/home
 * Được thiết kế theo pattern BFF (Backend For Frontend) để Mobile App
 * chỉ cần gọi 1 request duy nhất thay vì 4-5 request riêng lẻ.
 */
@Getter
@Setter
@Builder
public class CustomerHomeDashboardResponse {

    /** 1. Thông tin khách hàng */
    private ProfileSummary profile;

    /** 2. Danh sách xe đang active (ACTIVE status trong BookingDetail) */
    private List<VehicleCard> vehicles;

    /** 3. Phiên đỗ xe đang diễn ra (exitTime IS NULL), null nếu không có */
    private ActiveSessionCard activeSession;

    /** 4. Tổng hợp các mục đang chờ xử lý */
    private PendingActionSummary pendingActions;

    // ─────────────────────────────────────────────────────────────────────────
    // Nested DTOs (static inner class để gom chặt, không phải tạo file riêng)
    // ─────────────────────────────────────────────────────────────────────────

    @Getter
    @Setter
    @Builder
    public static class ProfileSummary {
        private Integer customerId;
        private String fullName;
        private String phone;
        private String groupName;   // Tên nhóm (căn hộ/công ty)
    }

    @Getter
    @Setter
    @Builder
    public static class VehicleCard {
        private Integer bookingDetailId;
        private String vehicleNo;           // Biển số xe
        private String packageName;         // Tên gói (Gói tháng Ô tô...)
        private Integer durationMonths;     // Thời hạn gói (tháng)
        private LocalDateTime endDate;      // Ngày hết hạn
        private long daysLeft;              // Số ngày còn lại (tính tại Backend)
        private String status;              // ACTIVE / EXPIRING_SOON / EXPIRED
    }

    @Getter
    @Setter
    @Builder
    public static class ActiveSessionCard {
        private Long sessionId;
        private String vehicleNo;           // Biển số xe đang trong bãi
        private LocalDateTime entryTime;    // Giờ vào bãi
        private String zoneInName;          // Cổng/khu vực vào
        private BigDecimal estimatedFee;    // Phí tạm tính hiện tại
    }

    @Getter
    @Setter
    @Builder
    public static class PendingActionSummary {
        private int draftCount;             // Xe đang trong giỏ chờ thanh toán
        private int pendingPaymentCount;    // Xe đang chờ xác nhận thanh toán
        private int expiringSoonCount;      // Xe có gói sắp hết hạn (< 7 ngày)
    }
}
