package com.smartparking.payment.service;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.payment.PaymentDetailResponse;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.specification.PaymentSpecs;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.entity.PackageVehicleType;
import com.smartparking.subscription.repository.PackagePriceRepository;
import com.smartparking.subscription.repository.PackageVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AdminPaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository; // Gọi thêm repo này để lấy node con
    private final ParkingSessionRepository parkingSessionRepository; // Gọi thêm repo này để lấy node con
    private final PackagePriceRepository packagePriceRepository; // Gọi thêm repo này để lấy node con
    private final BookingDetailRepository bookingDetailRepository; // Gọi thêm repo này để lấy node con
    private final PackageVehicleTypeRepository packageVehicleTypeRepository; // Gọi thêm repo này để lấy node con

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + id));
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentTreeResponse getPaymentTreeDetails(Long paymentId) {
        // 3.1. Lấy Node Cha
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + paymentId));

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
    public void resolvePayment(Long paymentId, Integer employeeId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // Chỉ cho phép duyệt những đơn đang bị kẹt
        Status currentStatus = payment.getStatus();
        if (currentStatus == Status.SUCCESS) {
            throw new RuntimeException("Giao dịch này đã được xử lý thành công từ trước!");
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
            }
            return;
        }


        List<BookingDetail> draftsToActive = details.stream()
                .map(PaymentDetail::getBookingDetail)
                .toList();

        // Lấy hàng loạt PackagePrice để biết khách mua mấy tháng
        List<Integer> packageIds = draftsToActive.stream()
                .map(BookingDetail::getPackagePriceId)
                .distinct()
                .toList();

        Map<Integer, PackagePrice> packageMap = packagePriceRepository.findAllById(packageIds).stream()
                .collect(Collectors.toMap(PackagePrice::getId, pp -> pp));

        LocalDateTime now = LocalDate.now().atStartOfDay();


        // đếm số lượng xe và tổng hợp lỗi
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
            throw new RuntimeException(fullErrorMessage);
        }

        payment.setStatus(Status.SUCCESS);
        paymentRepository.save(payment);

        for (BookingDetail draft : draftsToActive) {
            // Lấy gói cước để biết mua thêm mấy tháng
            PackagePrice packagePrice = packageMap.get(draft.getPackagePriceId());
            int duration = packagePrice.getDurationMonths();

            // Tìm ngày hết hạn xa nhất hiện tại của xe này trong DB
            Optional<LocalDateTime> maxEndDateOpt = bookingDetailRepository.findMaxEndDateByVehicleNo(draft.getVehicleNo(),BookingStatus.ACTIVE);
            LocalDateTime referenceEndDate = maxEndDateOpt.orElse(null);

            LocalDateTime newStartDate;

            // Nếu xe vẫn còn hạn -> Nối đuôi. Nếu xe đã hết hạn -> Tính từ bây giờ
            if (referenceEndDate != null && referenceEndDate.isAfter(now)) {
                newStartDate = referenceEndDate;
            } else {
                newStartDate = now;
            }

            // Ghi đè lại ngày bắt đầu và kết thúc mới (Không quan tâm lệch giá B2B)
            draft.setStartDate(newStartDate);
            draft.setEndDate(newStartDate.plusMonths(duration).minusSeconds(1));

            // Kích hoạt vé
            draft.setStatus(BookingStatus.ACTIVE);
        }

        bookingDetailRepository.saveAll(draftsToActive);
    }
}
