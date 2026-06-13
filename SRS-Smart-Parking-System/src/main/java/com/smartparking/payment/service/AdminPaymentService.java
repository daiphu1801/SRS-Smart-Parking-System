package com.smartparking.payment.service;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.PaymentReconciliationResponse;
import com.smartparking.payment.dto.response.payment.PaymentDetailResponse;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.specification.PaymentSpecs;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.service.SystemConfigService;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.entity.PackageVehicleType;
import com.smartparking.subscription.repository.PackagePriceRepository;
import com.smartparking.subscription.repository.PackageVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository; // Gọi thêm repo này để lấy node con
    private final ParkingSessionRepository parkingSessionRepository; // Gọi thêm repo này để lấy node con
    private final PackagePriceRepository packagePriceRepository; // Gọi thêm repo này để lấy node con
    private final BookingDetailRepository bookingDetailRepository; // Gọi thêm repo này để lấy node con
    private final PackageVehicleTypeRepository packageVehicleTypeRepository; // Gọi thêm repo này để lấy node con
    private final SystemConfigService systemConfigService;
    // 1. GET ALL THEO FILTER
    @Transactional(readOnly = true)
    public Page<PaymentResponse> searchPayments(PaymentFilterRequest filter, Pageable pageable) {

        Specification<Payment> spec = Specification
                .where(PaymentSpecs.hasCustomerId(filter.getCustomerId()))
                .and(PaymentSpecs.hasCustomerPhone(filter.getCustomerPhone()))
                .and(PaymentSpecs.hasSessionId(filter.getParkingSessionId()))
                .and(PaymentSpecs.hasPayCode(filter.getPayCode()))
                .and(PaymentSpecs.hasStatus(filter.getPayStatus()))
                .and(PaymentSpecs.hasGateway(filter.getGateway()))
                .and(PaymentSpecs.hasMethod(filter.getMethod()))
                .and(PaymentSpecs.hasAmountGreaterThanEqual(filter.getMinAmount()))
                .and(PaymentSpecs.hasAmountLessThanEqual(filter.getMaxAmount()))
                .and(PaymentSpecs.hasCreatedAtBetween(filter.getCreatedAtFrom(), filter.getCreatedAtTo()))
                .and(PaymentSpecs.hasUpdatedAtBetween(filter.getUpdatedAtFrom(), filter.getUpdatedAtTo()));

        return paymentRepository.findAll(spec, pageable)
                .map(this::mapToPaymentResponse);
    }

    // 2. GET CHI TIẾT CƠ BẢN
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Giao dịch với ID: " + id));
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentTreeResponse getPaymentTreeDetails(Long paymentId) {
        // 3.1. Lấy Node Cha
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Giao dịch với ID: " + paymentId));

        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(paymentId);

        return PaymentTreeResponse.builder()
                .paymentInfo(mapToPaymentResponse(payment))
                .details(details.stream().map(this::mapToPaymentDetailResponse).collect(Collectors.toList()))
                .build();
    }

    // --- HELPER MAPPERS ---

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .payerId(payment.getPayerId())
                .customerFullName(payment.getCustomer() != null ? payment.getCustomer().getFullName() : null)
                .customerPhone(payment.getCustomer() != null ? payment.getCustomer().getPhone() : null)
                .transactionId(payment.getTransactionId())
                .payCode(payment.getPayCode())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .gateway(payment.getGateway())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }



    private PaymentDetailResponse mapToPaymentDetailResponse(PaymentDetail detail) {
        return PaymentDetailResponse.builder()
                .id(detail.getId())
                .paymentId(detail.getPaymentId())
                .bookingDetailId(detail.getBookingDetailId())
                .itemAmount(detail.getItemAmount())
                .appliedStartDate(detail.getAppliedStartDate())
                .appliedEndDate(detail.getAppliedEndDate())
                .build();
    }

    @Transactional
    public void resolvePayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Giao dịch không tồn tại"));

        Status currentStatus = payment.getStatus();
        if (currentStatus == Status.SUCCESS || currentStatus == Status.MUST_RESOlVE) {
            throw new BusinessException("Giao dịch này đã được xử lý thành công từ trước!");
        }

        // Cập nhật hóa đơn thành SUCCESS
        // (Nếu có cột lưu người duyệt thì set luôn: payment.setResolvedBy(employeeId); )
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentIdWithBookingDetail(paymentId);

        if (details.isEmpty()) {
            Long sessionId = payment.getParkingSessionId();
            ParkingSession session = parkingSessionRepository.findById(sessionId).orElse(null);

            if (session != null) {
                payment.setStatus(Status.SUCCESS);
                paymentRepository.save(payment);

                BigDecimal paid = payment.getAmount();
                session.setAmountPaid((session.getAmountPaid() != null ? session.getAmountPaid() : BigDecimal.ZERO).add(paid));
                session.setAmountLeft(BigDecimal.ZERO); // Xóa nợ
                int gracePeriod = systemConfigService.getGracePeriodMinutes();

                session.setGracePeriodEnd(LocalDateTime.now().plusMinutes(gracePeriod));

                parkingSessionRepository.save(session);

                log.info("💳 MANUAL OVERRIDE: Đã duyệt thủ công thanh toán vé lượt cho Session ID {}", sessionId);
            }
            return;
        }


        List<BookingDetail> draftsToActive = details.stream()
                .map(PaymentDetail::getBookingDetail)
                .toList();

        List<Integer> packageIds = draftsToActive.stream()
                .map(BookingDetail::getPackagePriceId)
                .distinct()
                .toList();

        Map<Integer, PackagePrice> packageMap = packagePriceRepository.findAllById(packageIds).stream()
                .collect(Collectors.toMap(PackagePrice::getId, pp -> pp));

        LocalDateTime now = LocalDate.now().atStartOfDay();


        List<String> errorMessages = new ArrayList<>();

        for (BookingDetail draft : draftsToActive) {
            Integer groupId = draft.getBooking().getGroupId();
            PackagePrice packagePrice = packageMap.get(draft.getPackagePriceId());
            Integer pkgVehTypeId = packagePrice.getPkgVehTypeId();

            PackageVehicleType packageVehicleType = packageVehicleTypeRepository.findById(pkgVehTypeId)
                    .orElseThrow(() -> new RuntimeException("Dữ liệu gói cước bị lỗi!"));

            // Đếm xem CÁC XE KHÁC (trừ xe đang được duyệt) đã chiếm bao nhiêu slot rồi
            long otherVehiclesCount = bookingDetailRepository.countOtherDistinctVehiclesInUse(
                    pkgVehTypeId,
                    groupId,
                    Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED,BookingStatus.COMPLETE),
                    draft.getVehicleNo()
            );

            // Nếu xe khác đã lấp đầy slot -> KHÔNG THROW NGAY, MÀ THÊM VÀO LIST LỖI
            if (otherVehiclesCount >= packageVehicleType.getMaxQuantity()) {
                errorMessages.add("- Xe mang biển số " + draft.getVehicleNo() + " (Giới hạn: " + packageVehicleType.getMaxQuantity() + " slot)");
            }
        }
        if (!errorMessages.isEmpty()) {
            String fullErrorMessage = "Không thể duyệt! Bãi đỗ đã hết hạn ngạch cho các phương tiện sau:\n"
                    + String.join("\n", errorMessages)
                    + "\n\nVui lòng từ chối đơn này và tiến hành Hoàn tiền (Refund) cho khách!";
            throw new BusinessException(fullErrorMessage);
        }

        payment.setStatus(Status.SUCCESS);
        paymentRepository.save(payment);

        for (BookingDetail draft : draftsToActive) {
            LocalDateTime originalStartDate = draft.getStartDate();

            if (now.isAfter(originalStartDate)) {
                log.info("Trượt ngày kích hoạt cho xe {} do thanh toán trễ. Bắt đầu tính từ hôm nay.", draft.getVehicleNo());

                draft.setStartDate(now);

                PackagePrice packagePrice = packagePriceRepository.findById(draft.getPackagePriceId())
                        .orElseThrow(() -> new BusinessException("Không tìm thấy gói cước!"));
                int duration = packagePrice.getDurationMonths();

                draft.setEndDate(now.plusMonths(duration).minusSeconds(1));
            }

            if (draft.getStartDate().isAfter(now)) {
                draft.setStatus(BookingStatus.PENDING_ACTIVATION);
            } else {
                draft.setStatus(BookingStatus.ACTIVE);
            }
        }
        log.info("💳 MANUAL OVERRIDE: Đã duyệt thủ công thành công Giao dịch ID {} và kích hoạt {} vé tháng.", paymentId, draftsToActive.size());
        bookingDetailRepository.saveAll(draftsToActive);

    }

    @Transactional(readOnly = true)
    public Page<PaymentReconciliationResponse> getReconciliationExceptions(Pageable pageable) {


        Page<Payment> exceptions = paymentRepository.findReconciliationExceptions( pageable);

        return exceptions.map(this::mapToReconciliationResponse);
    }

    private PaymentReconciliationResponse mapToReconciliationResponse(Payment payment) {
        String flag;
        String warning;

        if (payment.getStatus() == Status.MANUAL_SUCCESS) {
            flag = "RED_FLAG";
            warning = "NGUY HIỂM: Bảo vệ đã xác nhận mở cổng nhưng quá thời gian không thấy tiền ngân hàng đổ về!";
        } else if (payment.getStatus() == Status.PARTIAL_PAYMENT) {
            flag = "YELLOW_FLAG";
            warning = "LỆCH TIỀN: Khách hàng chuyển khoản thiếu tiền, cần truy thu!";
        } else if (payment.getStatus() == Status.NEEDS_ATTENTION) {
            flag = "YELLOW_FLAG";
            warning = "LƯU Ý: Giao dịch có dấu hiệu bất thường từ Gateway, cần kiểm tra thủ công.";
        } else {
            // Đề phòng lọt các case khác
            flag = "YELLOW_FLAG";
            warning = "Giao dịch ngoại lệ.";
        }

        return PaymentReconciliationResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .payCode(payment.getPayCode())
                .parkingSessionId(payment.getParkingSessionId())
                .amount(payment.getAmount())
                .method(payment.getMethod() != null ? payment.getMethod().name() : "N/A")
                .status(payment.getStatus().name())
                .reconciliationFlag(flag)
                .warningMessage(warning)
                // Sếp có thể map thêm ngày giờ nếu muốn hiển thị trên giao diện
                .build();
    }

    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Giao dịch không tồn tại"));

        if (payment.getStatus() == Status.SUCCESS || payment.getStatus() == Status.CANCELED) {
            throw new BusinessException("Không thể hủy! Giao dịch này đã hoàn thành hoặc đã bị hủy từ trước.");
        }

        payment.setStatus(Status.CANCELED);
        paymentRepository.save(payment);

        List<PaymentDetail> details = paymentDetailRepository.findByPaymentIdWithBookingDetail(paymentId);

        if (!details.isEmpty()) {
            List<BookingDetail> draftsToCancel = details.stream()
                    .map(PaymentDetail::getBookingDetail)
                    .toList();

            for (BookingDetail draft : draftsToCancel) {
                draft.setStatus(BookingStatus.CANCELED);
            }

            bookingDetailRepository.saveAll(draftsToCancel);
            log.info("🚫 Đã hủy giao dịch {} và nhả slot cho {} hợp đồng vé tháng.", paymentId, draftsToCancel.size());
        } else {
            log.info("🚫 Đã hủy giao dịch thanh toán vé lượt cho Session ID {}", payment.getParkingSessionId());
        }
    }
}
