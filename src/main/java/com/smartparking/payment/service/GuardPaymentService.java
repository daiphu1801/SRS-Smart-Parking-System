package com.smartparking.payment.service;

import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.repository.TariffRuleRepository;
import com.smartparking.shared.config.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.smartparking.payment.service.SystemPaymentService.generatePayCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardPaymentService {
    private final ParkingSessionRepository parkingSessionRepository;
    private final PaymentRepository paymentRepository;
    private final BillingService billingService; // Lấy "Kế toán trưởng" vào làm việc
    private final SettingsService settingService; // Để lấy số phút ân hạn

    @Transactional
    public Map<String, Object> processCashPayment(Long sessionId, BigDecimal cashAmount) {
        log.info("Bắt đầu xử lý thu tiền mặt {} cho session: {}", cashAmount, sessionId);

        // 1. Tìm phiên đỗ xe
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

        LocalDateTime now = LocalDateTime.now();

        // 2. Kế toán tính tiền lại lần cuối trước khi thu
        billingService.calculateSessionFee(session, now);

        BigDecimal currentLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;

        if (currentLeft.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of("success", false, "message", "Cuốc xe này không còn nợ tiền, không cần thanh toán thêm!");
        }

        // 3. Khởi tạo và Lưu giao dịch thanh toán (Thành công luôn vì thu tiền mặt)
        String payCode = generatePayCode(); // Hàm sinh mã của ông
        Payment payment = Payment.builder()
                .payCode(payCode)
                .method(PaymentMethod.CASH)
                .amount(cashAmount)
                .gateway("GUARD_DESK")
                .status(Status.SUCCESS)
                .parkingSessionId(sessionId)
                .build();
        paymentRepository.save(payment);

        // 4. Tính toán trừ nợ cho Session
        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal updatedPaid = currentPaid.add(cashAmount);
        session.setAmountPaid(updatedPaid);

        BigDecimal newLeft = session.getAmountDue().subtract(updatedPaid);
        if (newLeft.compareTo(BigDecimal.ZERO) < 0) {
            newLeft = BigDecimal.ZERO; // Bẫy số dư
        }
        session.setAmountLeft(newLeft);

        // ==========================================
        // 5. NHÁNH 1: KHÁCH ĐÃ TRẢ ĐỦ (HOẶC THỪA)
        // ==========================================
        if (newLeft.compareTo(BigDecimal.ZERO) <= 0) {
            // Chỉ cấp Grace Period, tuyệt đối không đụng chạm đến Barrier
            session.setGracePeriodEnd(now.plusMinutes(settingService.getGracePeriodMinutes()));
            parkingSessionRepository.save(session);

            return Map.of(
                    "success", true,
                    "message", "Thanh toán thành công. Hệ thống đã cấp thời gian ân hạn.",
                    "amountLeft", BigDecimal.ZERO
            );
        }

        // ==========================================
        // 6. NHÁNH 2: KHÁCH TRẢ THIẾU
        // ==========================================
        parkingSessionRepository.save(session);
        return Map.of(
                "success", true,
                "message", "Đã thu một phần. Xe vẫn còn nợ: " + newLeft,
                "amountLeft", newLeft
        );
    }

}