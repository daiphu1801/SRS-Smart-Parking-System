package com.smartparking.payment.service;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.payment.dto.request.payment.PaymentSessionRequest;
import com.smartparking.payment.dto.request.payment.SepayWebhookRequest;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.specification.PaymentSpecs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SystemPaymentService {
    // @Value("${sepay.bank-account}")
    // private String bankAccount;
    //
    // @Value("${sepay.bank-name}")
    // private String bankName;
    //
    // @Value("${sepay.account-name}")
    // private String accountName;

    @Value("${app.sepay.webhook-secret}")
    @Getter
    private String webhookSecret;

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    private final BillingService billingService;

    // --- 3. WEBHOOK XỬ LÝ KHI CÓ TIỀN VỀ ---
    @Transactional
    public void processSepayWebhook(SepayWebhookRequest request) {

        String transferContent = request.getContent();
        BigDecimal amountPaid = request.getTransferAmount();

        List<Payment> matchedPayments = paymentRepository.findPaymentByTransferContent(transferContent);

        if (matchedPayments.isEmpty()) {
            return;
        }

        Payment matchedPayment = matchedPayments.getFirst();

        // chặn idempotency
        Status currentStatus = matchedPayment.getStatus();
        if (currentStatus != Status.PENDING &&
                currentStatus != Status.EXPIRED &&
                currentStatus != Status.CANCELED) {
            return;
        }

        // 2. Xác định trạng thái của giao dịch (KHÔNG DÙNG RETURN ĐỂ LUỒNG ĐI TIẾP)
        if (amountPaid.compareTo(matchedPayment.getAmount()) < 0) {
            matchedPayment.setStatus(Status.PARTIAL_PAYMENT);
        } else if (currentStatus == Status.EXPIRED || currentStatus == Status.CANCELED) {
            matchedPayment.setStatus(Status.NEEDS_ATTENTION);
        } else {
            matchedPayment.setStatus(Status.SUCCESS);
        }
        matchedPayment.setGateway(request.getGateway());
        matchedPayment.setTransactionId(request.getTransactionId());
        // Lưu PaymenttransactionId
        paymentRepository.save(matchedPayment);

        // 3. Truyền trạng thái xuống để đồng bộ với Vé hoặc Session
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(matchedPayment.getId());

        if (!details.isEmpty()) {
            activateMonthlyBookings(details, matchedPayment.getStatus()); // <--- Truyền status vào đây
        } else {
            releaseParkingSession(matchedPayment, amountPaid); // <--- Truyền status vào đây
        }
    }

    private void activateMonthlyBookings(List<PaymentDetail> details, Status status) {
        LocalDateTime now = LocalDate.now().atStartOfDay();

        List<BookingDetail> draftsToActive = details.stream()
                .map(PaymentDetail::getBookingDetail)
                .toList();

        for (BookingDetail draft : draftsToActive) {
            // Nếu Hóa đơn bị LỖI (Thiếu tiền hoặc Quá hạn) -> Vé cũng báo lỗi Needs

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
        // 1. Tự bóc ID từ object payment ra
        Long sessionId = payment.getParkingSessionId();

        // (Tuỳ chọn) Đề phòng dữ liệu lỗi, payment không trỏ tới session nào
        if (sessionId == null) {
            return;
        }

        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

        // ==========================================
        // KIỂM TRA: XE ĐÃ RA KHỎI BÃI CHƯA?
        // ==========================================
        if (session.getExitTime() != null) {
            payment.setStatus(Status.REFUND_PENDING);
            paymentRepository.save(payment);
            return;
        }

        // ==========================================
        // KIỂM TRA: NỢ ĐÃ ĐƯỢC TRẢ TRƯỚC ĐÓ CHƯA?
        // ==========================================
        BigDecimal currentLeft = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;
        if (currentLeft.compareTo(BigDecimal.ZERO) <= 0) {
            payment.setStatus(Status.REFUND_PENDING);
            paymentRepository.save(payment);
            return;
        }

        // ==========================================
        // TÍNH TOÁN TRỪ NỢ VÀ GIA HẠN
        // ==========================================
        BigDecimal currentPaid = session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal totalDue = session.getAmountDue() != null ? session.getAmountDue() : BigDecimal.ZERO;

        BigDecimal updatedPaid = currentPaid.add(newlyPaidAmount);
        session.setAmountPaid(updatedPaid);

        BigDecimal newLeft = totalDue.subtract(updatedPaid);
        if (newLeft.compareTo(BigDecimal.ZERO) < 0) {
            newLeft = BigDecimal.ZERO;
        }
        session.setAmountLeft(newLeft);

        Status originalStatus = payment.getStatus();
        if ((originalStatus == Status.SUCCESS || originalStatus == Status.NEEDS_ATTENTION)
                && newLeft.compareTo(BigDecimal.ZERO) <= 0) {
            session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(15)); // Hoặc dùng cái global variable của ông
        }

        parkingSessionRepository.save(session);
    }

    // --- 4. CHECK TRẠNG THÁI ---
    @Transactional(readOnly = true)
    public String checkPaymentStatus(String payCode) {
        Specification<Payment> payCodeSpec = PaymentSpecs.hasPayCode(payCode);

        // 2. Dùng findOne thay cho findBy...
        Payment payment = paymentRepository.findOne(payCodeSpec)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
        return payment.getStatus().name(); // Trả về PENDING, SUCCESS, CANCELED...
    }

    @Transactional
    public PaymentInitiateResponse initiateSessionPayment(PaymentSessionRequest request) {

        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

        billingService.calculateSessionFee(session, LocalDateTime.now());

        BigDecimal sessionFee = session.getAmountLeft() != null ? session.getAmountLeft() : BigDecimal.ZERO;
        if (sessionFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Cuốc xe này đã được thanh toán hoặc không phát sinh phí.");
        }

        String payCode = generatePayCode();
        Payment payment = Payment.builder()
                .payCode(payCode)
                .method(request.getMethod())
                .amount(sessionFee)
                .gateway(request.getGateway())
                .status(Status.PENDING)
                .parkingSessionId(session.getId())
                .build();

        paymentRepository.save(payment);

        String checkoutUrl = "https://vnpay.vn/mock-checkout-url?code=" + payCode;

        return PaymentInitiateResponse.builder()
                .payCode(payCode)
                .amount(sessionFee)
                .checkoutUrl(checkoutUrl)
                .build();
    }

    public static String generatePayCode() {
        // Gen mã giao dịch đẹp đẹp chút (VD: SP-UUID)
        return "SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}