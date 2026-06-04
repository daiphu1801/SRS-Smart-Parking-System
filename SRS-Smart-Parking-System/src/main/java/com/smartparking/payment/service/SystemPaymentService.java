package com.smartparking.payment.service;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.service.system.IoTService;
import com.smartparking.payment.dto.request.payment.PaymentSessionRequest;
import com.smartparking.payment.dto.request.payment.SepayWebhookRequest;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.specification.PaymentSpecs;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.kafka.dto.ParkingSessionEvent;
import com.smartparking.shared.service.PaymentGatewayService;
import com.smartparking.shared.service.SystemConfigService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemPaymentService {

    @Value("${app.sepay.webhook-secret}")
    @Getter
    private String webhookSecret;
    @Value("${app.sepay.webhook-secret}")
    private String sepaySecretKey;

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final Map<String, PaymentGatewayService> gatewayServices;
    private final IoTService iotService;
    private final BillingService billingService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemConfigService systemConfigService;
    /**
     * Processes incoming webhook IPN from the banking gateway (e.g., Sepay).
     * Handles idempotency, partial payments, and delayed transactions.
     *
     * @param request The webhook payload containing transfer details.
     */
    @Transactional
    public void processSepayWebhook(SepayWebhookRequest request) {

        String transferContent = request.getContent();
        BigDecimal amountPaid = request.getTransferAmount();

        List<Payment> matchedPayments = paymentRepository.findPaymentByTransferPaycode(transferContent);

        if (matchedPayments.isEmpty()) {
            log.warn("⚠️ WEBHOOK ALERT: Nhận được tiền ({} VNĐ) nhưng KHÔNG tìm thấy mã đơn hàng phù hợp! Nội dung CK: {}", amountPaid, transferContent);
            return;
        }

        Payment matchedPayment = matchedPayments.getFirst();

        Status currentStatus = matchedPayment.getStatus();
        if (currentStatus != Status.PENDING &&
                currentStatus != Status.EXPIRED &&
                currentStatus != Status.CANCELED) {
            log.info("Webhook Idempotency: Bỏ qua xử lý do Giao dịch {} đã ở trạng thái {}", matchedPayment.getPayCode(), currentStatus);
            return;
        }

        if (matchedPayment.getParkingSessionId() != null) {
            boolean isManuallyResolved = paymentRepository.existsByParkingSessionIdAndAmountAndStatus(
                    matchedPayment.getParkingSessionId(),
                    amountPaid,
                    Status.MANUAL_SUCCESS
            );

            if (isManuallyResolved) {
                log.info("WEBHOOK RECONCILED: Giao dịch {} về trễ. Bảo vệ đã xác nhận tay trước đó. Chốt OVERRIDDEN_SUCCESS!", matchedPayment.getPayCode());
                matchedPayment.setStatus(Status.OVERRIDDEN_SUCCESS);
                matchedPayment.setGateway("SEPAY");
                matchedPayment.setTransactionId(request.getGateway() + "-" + request.getTransactionId());
                paymentRepository.save(matchedPayment);

                return;
            }
        }

        if (amountPaid.compareTo(matchedPayment.getAmount()) < 0) {
            matchedPayment.setStatus(Status.PARTIAL_PAYMENT);
            log.warn("WEBHOOK ALERT: Khách thanh toán THIẾU TIỀN cho mã {}. Cần thu: {}, Đã chuyển: {}",
                    matchedPayment.getPayCode(), matchedPayment.getAmount(), amountPaid);
        } else if (currentStatus == Status.EXPIRED || currentStatus == Status.CANCELED) {
            log.warn("WEBHOOK ALERT: Khách thanh toán MUỘN cho mã {} (Đơn đã Expired/Canceled). Cần Admin xử lý tay!", matchedPayment.getPayCode());
            matchedPayment.setStatus(Status.NEEDS_ATTENTION);
        } else {
            matchedPayment.setStatus(Status.SUCCESS);
            log.info("WEBHOOK SUCCESS: Nhận thành công {} VNĐ cho mã {}", amountPaid, matchedPayment.getPayCode());
        }
        matchedPayment.setGateway(request.getGateway());
        matchedPayment.setTransactionId(request.getTransactionId());
        paymentRepository.save(matchedPayment);

        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(matchedPayment.getId());

        try {
            if (!details.isEmpty()) {
                activateMonthlyBookings(details, matchedPayment.getStatus());
            } else {
                releaseParkingSession(matchedPayment, amountPaid);
            }
        } catch (Exception e) {
            log.error("CRITICAL: Đã nhận tiền cho mã {} nhưng LỖI XỬ LÝ NGHIỆP VỤ: {}", matchedPayment.getPayCode(), e.getMessage());
            throw e;
        }
    }

    private void activateMonthlyBookings(List<PaymentDetail> details, Status status) {
        LocalDateTime now = LocalDate.now().atStartOfDay();

        List<BookingDetail> draftsToActive = details.stream()
                .map(PaymentDetail::getBookingDetail)
                .toList();

        for (BookingDetail draft : draftsToActive) {
            if (status == Status.NEEDS_ATTENTION) {
                draft.setStatus(BookingStatus.NEEDS_ATTENTION);
            }

            else if (status == Status.SUCCESS) {
                if (draft.getStartDate() != null && draft.getStartDate().isAfter(now)) {
                    draft.setStatus(BookingStatus.PENDING_ACTIVATION);
                } else {
                    draft.setStatus(BookingStatus.ACTIVE);
                }
            } else if (status == Status.PARTIAL_PAYMENT) {
                draft.setStatus(BookingStatus.PARTIAL_PAYMENT);
            }
        }

        bookingDetailRepository.saveAll(draftsToActive);
    }

    private void releaseParkingSession(Payment payment, BigDecimal newlyPaidAmount) {
        Long sessionId = payment.getParkingSessionId();
        if (sessionId == null) {
            return;
        }

        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Session không tồn tại"));

        if (session.getExitTime() != null) {
            payment.setStatus(Status.REFUND_PENDING);
            paymentRepository.save(payment);
            return;
        }
        String vehicleNo = session.getVehicleNo();
        String redisKey = iotService.getRedisKey(vehicleNo);
        ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);

        // Fallback to database values as the source of truth
        BigDecimal currentLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;
        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal totalDue = session.getAmountDue() != null ? session.getAmountDue() : BigDecimal.ZERO;

        // Override with Cache values if available for real-time accuracy
        if (cachedSession != null) {
            log.info("[🔍 CACHE HIT] Webhook ưu tiên chốt số từ Redis cho xe {}", vehicleNo);
            if (cachedSession.getAmountLeft() != null) {
                currentLeft = new BigDecimal(cachedSession.getAmountLeft().toString());
            }
            if (cachedSession.getAmountPaid() != null) {
                currentPaid = new BigDecimal(cachedSession.getAmountPaid().toString());
            }
            if (cachedSession.getAmountDue() != null) {
                totalDue = new BigDecimal(cachedSession.getAmountDue().toString());
            }
        } else {
            log.warn("[⚠️ CACHE MISS] Redis trống lúc Webhook về, dùng gốc tiền từ DB cho xe {}", vehicleNo);
        }


        if (currentLeft.compareTo(BigDecimal.ZERO) <= 0) {
            payment.setStatus(Status.REFUND_PENDING);
            paymentRepository.save(payment);
            return;
        }

        BigDecimal updatedPaid = currentPaid.add(newlyPaidAmount);
        session.setAmountPaid(updatedPaid);

        BigDecimal newLeft = totalDue.subtract(updatedPaid);
        if (newLeft.compareTo(BigDecimal.ZERO) < 0) {
            newLeft = BigDecimal.ZERO;
        }
        session.setAmountLeft(newLeft);
        int gracePeriod = systemConfigService.getGracePeriodMinutes();

        Status originalStatus = payment.getStatus();
        if ((originalStatus == Status.SUCCESS || originalStatus == Status.NEEDS_ATTENTION)
                && newLeft.compareTo(BigDecimal.ZERO) <= 0) {
            session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(gracePeriod)); // Extend grace period to allow vehicle exit after successful payment
        }

        parkingSessionRepository.save(session);
        iotService.syncSessionToCache(session);
    }

    @Transactional(readOnly = true)
    public String checkPaymentStatus(String payCode) {
        Specification<Payment> payCodeSpec = PaymentSpecs.hasPayCode(payCode);

        Payment payment = paymentRepository.findOne(payCodeSpec)
                .orElseThrow(() -> new BusinessException("Giao dịch không tồn tại"));
        return payment.getStatus().name(); // Returns standard status strings (e.g., PENDING, SUCCESS, CANCELED)
    }

    @Transactional
    public PaymentInitiateResponse initiateSessionPayment(PaymentSessionRequest request) {
        String vehicleNo = request.getVehicleNo();
        String redisKey = iotService.getRedisKey(vehicleNo);

        ParkingSessionEvent cachedSession = (ParkingSessionEvent) redisTemplate.opsForValue().get(redisKey);
        Long sessionId = cachedSession.getId();

        ParkingSession session;


        if (sessionId != null) {
            session = parkingSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new BusinessException("Session không tồn tại trong hệ thống"));
        } else {
            session = parkingSessionRepository.findFirstByVehicleNoAndExitTimeIsNullOrderByIdDesc(vehicleNo)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy xe " + vehicleNo + " đang đỗ trong bãi"));
        }

        boolean cacheHit = (cachedSession != null && cachedSession.getExitTime() == null);
        if (cacheHit) {
            if (cachedSession.getAmountPaid() != null) session.setAmountPaid(new BigDecimal(cachedSession.getAmountPaid().toString()));
            if (cachedSession.getAmountDue() != null) session.setAmountDue(new BigDecimal(cachedSession.getAmountDue().toString()));
            if (cachedSession.getAmountLeft() != null) session.setAmountLeft(new BigDecimal(cachedSession.getAmountLeft().toString()));
            if (cachedSession.getGracePeriodEnd() != null) session.setGracePeriodEnd(LocalDateTime.parse(cachedSession.getGracePeriodEnd()));
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isGracePeriodExpired = session.getGracePeriodEnd() == null || now.isAfter(session.getGracePeriodEnd());

        if (isGracePeriodExpired) {
            billingService.calculateSessionFee(session, now);
            int gracePeriod = systemConfigService.getGracePeriodMinutes();
            session.setGracePeriodEnd(now.plusMinutes(gracePeriod));
            session = parkingSessionRepository.save(session);
            iotService.syncSessionToCache(session);
        } else {
            log.info("⏳ Xe {} đang trong thời gian ân hạn Kiosk, giữ nguyên giá cũ.", vehicleNo);
            if (!cacheHit) {
                iotService.syncSessionToCache(session);
            }
        }


        BigDecimal sessionFee = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;
        if (sessionFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cuốc xe này đã được thanh toán hoặc không phát sinh phí.");
        }

        Integer pendingPayments = paymentRepository.cancelPendingPayments(session.getId(), Status.PENDING, Status.CANCELED);

        String payCode = generatePayCode();
        Payment payment = Payment.builder()
                .payCode(payCode)
                .method(request.getMethod())
                .amount(sessionFee)
                .gateway(request.getGateway())
                .status(Status.PENDING)
                .parkingSessionId(session.getId())
                .build();

        try {
            paymentRepository.save(payment);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("[❌ PAYMENT FAILED] Không thể tạo lệnh thanh toán cho Session {} vì DB chưa đồng bộ xong.", session.getId(), e);
            throw new BusinessException("Phiên đỗ xe đang được hệ thống xử lý ngầm, vui lòng chờ trong giây lát hoặc ra cổng bảo vệ để thanh toán trực tiếp.");
        } catch (Exception e) {
            log.error("[❌ PAYMENT UNKNOWN ERROR]", e);
            throw new BusinessException("Hệ thống thanh toán đang bận, vui lòng thử lại sau hoặc thanh toán trực tiếp tại cổng.");
        }

        PaymentGatewayService selectedGateway = gatewayServices.get(request.getGateway().toUpperCase());
        if (selectedGateway == null) {
            throw new BusinessException("Hệ thống chưa hỗ trợ cổng thanh toán: " + request.getGateway());
        }

        String checkoutUrl = selectedGateway.generateCheckoutUrl(sessionFee, payCode);

        log.info("TẠO LỆNH THU: Vé lượt Session {} - Mã {} - Số tiền: {}", session.getId(), payCode, sessionFee);

        return PaymentInitiateResponse.builder()
                .payCode(payCode)
                .amount(sessionFee)
                .checkoutUrl(checkoutUrl)
                .build();
    }

    public static String generatePayCode() {
        return "SP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}