package com.smartparking.operation.service.customer;

import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.service.admin.CustomerService;
import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.response.CustomerHomeDashboardResponse;
import com.smartparking.operation.dto.response.CustomerHomeDashboardResponse.*;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service tổng hợp dữ liệu cho màn Home của Customer App.
 * <p>
 * Triết lý thiết kế (BFF – Backend For Frontend):
 * - Gom toàn bộ logic truy vấn tại đây, Flutter chỉ consume 1 DTO duy nhất.
 * - Scope bảo mật được đảm bảo tuyệt đối qua customerId từ JWT Principal.
 * - Logic tính toán (daysLeft, status label, estimatedFee) nằm ở Backend,
 *   không để Mobile tự tính để tránh phải update App khi business rule thay đổi.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerHomeDashboardService {

    /** Ngưỡng cảnh báo sắp hết hạn (ngày) */
    private static final int EXPIRING_SOON_THRESHOLD_DAYS = 7;

    private final CustomerService customerService;
    private final BookingDetailRepository bookingDetailRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tổng hợp toàn bộ dữ liệu cần thiết cho màn Home.
     *
     * @param customerId ID của customer đang đăng nhập (lấy từ JWT Principal)
     * @return {@link CustomerHomeDashboardResponse} – DTO tổng hợp gửi về Mobile
     */
    @Transactional(readOnly = true)
    public CustomerHomeDashboardResponse buildDashboard(Integer customerId) {
        // 1. Lấy thông tin profile khách hàng
        CustomerResponse customer = customerService.getCustomersById(customerId);

        // 2. Lấy danh sách xe đang ACTIVE, sắp xếp theo endDate ASC (sắp hết hạn lên đầu)
        List<BookingDetailDto> activeDetails =
                bookingDetailRepository.findDtoByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);

        // 3. Map sang VehicleCard (tính daysLeft và status label tại đây)
        List<VehicleCard> vehicleCards = activeDetails.stream()
                .map(this::mapToVehicleCard)
                .toList();

        // 4. Tìm phiên đỗ xe đang mở (exitTime IS NULL) – scope đúng customerId
        ActiveSessionCard activeSession = findActiveSession(customerId);

        // 5. Tổng hợp pending actions
        PendingActionSummary pendingActions = buildPendingActionSummary(customerId, vehicleCards);

        return CustomerHomeDashboardResponse.builder()
                .profile(buildProfileSummary(customer))
                .vehicles(vehicleCards)
                .activeSession(activeSession)
                .pendingActions(pendingActions)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private ProfileSummary buildProfileSummary(CustomerResponse customer) {
        return ProfileSummary.builder()
                .customerId(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .groupName(customer.getGroupName())
                .build();
    }

    /**
     * Map BookingDetailDto → VehicleCard với tính toán daysLeft và status label.
     *
     * Status logic:
     * - EXPIRED     : endDate đã qua
     * - EXPIRING_SOON : còn <= 7 ngày
     * - ACTIVE      : còn > 7 ngày
     */
    private VehicleCard mapToVehicleCard(BookingDetailDto dto) {
        LocalDateTime now = LocalDateTime.now();
        long daysLeft = dto.getEndDate() != null
                ? ChronoUnit.DAYS.between(now.toLocalDate(), dto.getEndDate().toLocalDate())
                : 0;

        String statusLabel;
        if (daysLeft < 0) {
            statusLabel = "EXPIRED";
        } else if (daysLeft <= EXPIRING_SOON_THRESHOLD_DAYS) {
            statusLabel = "EXPIRING_SOON";
        } else {
            statusLabel = "ACTIVE";
        }

        return VehicleCard.builder()
                .bookingDetailId(dto.getId())
                .vehicleNo(dto.getVehicleNo())
                .packageName(dto.getPackagePriceName())
                .durationMonths(dto.getDurationMonths())
                .endDate(dto.getEndDate())
                .daysLeft(Math.max(daysLeft, 0))  // không hiển thị số âm
                .status(statusLabel)
                .build();
    }

    /**
     * Tìm phiên đỗ đang mở. Trả về phiên mới nhất nếu có nhiều (edge case hệ thống).
     * Tính phí tạm tính dựa trên amountDue hiện tại trong DB (do AI Camera cập nhật).
     */
    private ActiveSessionCard findActiveSession(Integer customerId) {
        List<ParkingSession> openSessions =
                parkingSessionRepository.findOpenSessionsByCustomerId(customerId);

        if (openSessions.isEmpty()) {
            return null;
        }

        // Lấy phiên mới nhất (ORDER BY entryTime DESC đã có trong query)
        ParkingSession session = openSessions.getFirst();

        return ActiveSessionCard.builder()
                .sessionId(session.getId())
                .vehicleNo(session.getVehicleNo())
                .entryTime(session.getEntryTime())
                .zoneInName(session.getZoneIn() != null ? session.getZoneIn().getZoneName() : null)
                .estimatedFee(session.getAmountDue() != null ? session.getAmountDue() : BigDecimal.ZERO)
                .build();
    }

    /**
     * Tổng hợp PendingActionSummary:
     * - draftCount: xe trong giỏ hàng (DRAFT)
     * - pendingPaymentCount: xe đang chờ xác nhận thanh toán (PENDING_PAYMENT)
     * - expiringSoonCount: xe ACTIVE còn <= 7 ngày (đã tính từ vehicleCards)
     */
    private PendingActionSummary buildPendingActionSummary(
            Integer customerId, List<VehicleCard> vehicleCards) {

        long pendingCount = bookingDetailRepository.countByCustomerIdAndStatusIn(
                customerId,
                List.of(BookingStatus.DRAFT, BookingStatus.PENDING_PAYMENT)
        );

        long expiringSoonCount = vehicleCards.stream()
                .filter(v -> "EXPIRING_SOON".equals(v.getStatus()))
                .count();

        // Chia draft vs pendingPayment
        long draftCount = bookingDetailRepository.countByCustomerIdAndStatusIn(
                customerId, List.of(BookingStatus.DRAFT)
        );
        long pendingPaymentCount = bookingDetailRepository.countByCustomerIdAndStatusIn(
                customerId, List.of(BookingStatus.PENDING_PAYMENT)
        );

        return PendingActionSummary.builder()
                .draftCount((int) draftCount)
                .pendingPaymentCount((int) pendingPaymentCount)
                .expiringSoonCount((int) expiringSoonCount)
                .build();
    }
}
