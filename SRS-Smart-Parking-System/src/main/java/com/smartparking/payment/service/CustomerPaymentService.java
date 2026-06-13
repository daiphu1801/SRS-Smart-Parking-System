package com.smartparking.payment.service;

import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.payment.dto.BillingResult;
import com.smartparking.payment.dto.request.CheckoutRequest;
import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.response.PaymentCheckoutResponse;
import com.smartparking.payment.dto.response.payment.PaymentDetailResponse;
import com.smartparking.payment.dto.response.payment.PaymentInitiateResponse;
import com.smartparking.payment.dto.response.payment.PaymentResponse;
import com.smartparking.payment.dto.response.payment.PaymentTreeResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.PaymentDetail;
import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.Status;
import com.smartparking.payment.repository.PaymentDetailRepository;
import com.smartparking.payment.repository.PaymentRepository;
import com.smartparking.payment.specification.PaymentSpecs;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.service.PaymentGatewayService;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.repository.PackagePriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.smartparking.payment.service.SystemPaymentService.generatePayCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PackagePriceRepository packagePriceRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final Map<String, PaymentGatewayService> gatewayServices;


    @Value("${app.sepay.bank-account}")
    private String bankAccount;

    @Value("${app.sepay.bank-name}")
    private String bankName;

    @Value("${app.sepay.account-name}")
    private String accountName;


    /**
     * Retrieves a paginated list of payments for the current customer based on specific filters.
     *
     * @param myGroupIds The list of authorized group IDs the customer belongs to.
     * @param filter     The search criteria (payCode, gateway, date range).
     * @param pageable   Pagination information.
     * @return A paginated list of PaymentResponse objects.
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(List<Integer> myGroupIds, PaymentFilterRequest filter, Pageable pageable) {

        Specification<Payment> spec = Specification
                .where(PaymentSpecs.belongsToGroupIds(myGroupIds))
                .and(PaymentSpecs.isNotTrash())
                .and(PaymentSpecs.hasPayCode(filter.getPayCode()))
                .and(PaymentSpecs.hasGateway(filter.getGateway()))
                .and(PaymentSpecs.hasCreatedAtBetween(filter.getCreatedAtFrom(), filter.getCreatedAtTo()));

        return paymentRepository.findAll(spec, pageable).map(this::mapToPaymentResponse);
    }

    @Transactional(readOnly = true)
    public PaymentTreeResponse getMyPaymentTreeDetails(Long paymentId, List<Integer> myGroupIds) {

        Specification<Payment> secureSpec = Specification
                .where(PaymentSpecs.belongsToGroupIds(myGroupIds))
                .and((root, query, cb) -> cb.equal(root.get("id"), paymentId));

        Payment payment = paymentRepository.findOne(secureSpec)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch hoặc bạn không có quyền truy cập!"));

        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(paymentId);

        return PaymentTreeResponse.builder()
                .paymentInfo(mapToPaymentResponse(payment))
                .details(details.stream().map(this::mapToPaymentDetailResponse).collect(Collectors.toList()))
                .build();
    }


    /**
     * Cancels a pending payment transaction and releases any associated draft bookings.
     *
     * @param paymentId  The unique identifier of the payment to be canceled.
     * @param myGroupIds The list of group IDs the current user has access to (used for IDOR protection).
     * @throws BusinessException If the payment is not found, access is denied, or the payment is not in a PENDING state.
     */
    @Transactional
    public void cancelPayment(Long paymentId, List<Integer> myGroupIds) {

        // Enforce IDOR protection: Verify the payment exists and belongs to the user's authorized groups
        Specification<Payment> secureSpec = Specification
                .where(PaymentSpecs.belongsToGroupIds(myGroupIds))
                .and((root, query, cb) -> cb.equal(root.get("id"), paymentId));

        Payment payment = paymentRepository.findOne(secureSpec)
                .orElseThrow(() -> new BusinessException("Giao dịch không tồn tại hoặc bạn không có quyền truy cập!"));

        // Enforce business invariant: Only PENDING transactions can be canceled
        if (payment.getStatus() != Status.PENDING) {
            throw new BusinessException("Chỉ có thể hủy giao dịch đang chờ thanh toán!");
        }

        payment.setStatus(Status.CANCELED);
        paymentRepository.save(payment);

        // Cascade cancellation: Release associated draft bookings to free up parking slots
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(paymentId);
        if (!details.isEmpty()) {
            List<Integer> draftBookingIds = details.stream()
                    .map(detail -> detail.getBookingDetail().getId())
                    .toList();

            bookingDetailRepository.updateStatusToCanceled(draftBookingIds);
        }
    }


    private PaymentResponse mapToPaymentResponse(Payment payment) {
        String checkoutUrl = null;
        if (payment.getStatus() == Status.PENDING && payment.getGateway() != null) {
            PaymentGatewayService selectedGateway = gatewayServices.get(payment.getGateway());
            if (selectedGateway != null) {
                checkoutUrl = selectedGateway.generateCheckoutUrl(payment.getAmount(), payment.getPayCode());
            }
        }

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
                .checkoutUrl(checkoutUrl)
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


    public BillingResult calculateBilling(LocalDateTime referenceEndDate, PackagePrice packagePrice, boolean isSynchronize) {
        LocalDateTime now = LocalDate.now().atStartOfDay();

        int duration = packagePrice.getDurationMonths();
        BigDecimal basePrice = packagePrice.getPrice();

        LocalDateTime newStartDate;
        if (referenceEndDate != null && referenceEndDate.isAfter(now)) {
            newStartDate = referenceEndDate;
            LocalDateTime newEndDate = newStartDate.plusMonths(duration).minusSeconds(1);
            return new BillingResult(newStartDate, newEndDate, basePrice);
        }

        newStartDate = now;

        if (!isSynchronize) {

            LocalDateTime newEndDate = newStartDate.plusMonths(duration).minusSeconds(1);
            return new BillingResult(newStartDate, newEndDate, basePrice);

        } else {
            LocalDateTime newEndDate = now.withDayOfMonth(1).plusMonths(duration).minusSeconds(1);

            BigDecimal pricePerMonth = basePrice.divide(BigDecimal.valueOf(duration), 2, RoundingMode.HALF_UP);
            int daysInCurrentMonth = now.toLocalDate().lengthOfMonth();
            int remainingDays = daysInCurrentMonth - now.getDayOfMonth() + 1; // Inclusive of current day

            BigDecimal partialMonthPrice = pricePerMonth
                    .multiply(BigDecimal.valueOf(remainingDays))
                    .divide(BigDecimal.valueOf(daysInCurrentMonth), 0, RoundingMode.DOWN); // Round down, strictly integer logic

            BigDecimal fullMonthsPrice = pricePerMonth.multiply(BigDecimal.valueOf(duration - 1))
                    .setScale(0, RoundingMode.DOWN);

            BigDecimal finalPrice = partialMonthPrice.add(fullMonthsPrice);

            return new BillingResult(newStartDate, newEndDate, finalPrice);
        }
    }


    @Transactional
    public PaymentCheckoutResponse checkoutBookingDetails(Integer customerId, CheckoutRequest request) {

        List<Integer> detailIds = request.getBookingDetailIds();
        if (detailIds == null || detailIds.isEmpty()) {
            throw new BusinessException("Danh sách thanh toán không được để trống!");
        }

        List<BookingDetail> drafts = bookingDetailRepository.findAllByIdWithLock(detailIds);

        if (drafts.size() != detailIds.size()) {
            throw new BusinessException("Một số gói đăng ký không tồn tại hoặc đã bị xóa khỏi hệ thống!");
        }

        List<BookingDetail> expiredDrafts = new ArrayList<>();
        List<BookingDetail> validDrafts = new ArrayList<>();
        LocalDateTime now = LocalDate.now().atStartOfDay();

        for (BookingDetail draft : drafts) {
            if (!draft.getCustomer().getId().equals(customerId)) {
                throw new BusinessException("Phát hiện gói đăng ký không hợp lệ. Giao dịch bị hủy!");
            }
            if (draft.getStatus() != BookingStatus.DRAFT) {
                throw new BusinessException("Gói đăng ký cho xe " + draft.getVehicleNo() + " đã thay đổi trạng thái. Vui lòng kiểm tra lại giỏ hàng!");
            }

            if (draft.getStartDate().isBefore(now)) {
                draft.setStatus(BookingStatus.EXPIRED);
                expiredDrafts.add(draft);
            } else {
                validDrafts.add(draft);
            }
        }
        if (!expiredDrafts.isEmpty()) {
            bookingDetailRepository.saveAll(expiredDrafts);
            throw new BusinessException("Có gói cước trong giỏ hàng đã quá hạn kích hoạt. Hệ thống đã tự động cập nhật lại trạng thái, vui lòng kiểm tra và thanh toán lại!");
        }

        if (validDrafts.isEmpty()) {
            throw new BusinessException("Tất cả các gói cước được chọn đã trễ ngày kích hoạt. Vui lòng tạo đăng ký mới!");
        }

        List<Integer> packageIds = validDrafts.stream()
                .map(BookingDetail::getPackagePriceId)
                .distinct()
                .toList();

        Map<Integer, PackagePrice> packageMap = packagePriceRepository.findAllById(packageIds).stream()
                .collect(Collectors.toMap(PackagePrice::getId, pp -> pp));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BigDecimal> itemPrices = new ArrayList<>();

        for (BookingDetail draft : validDrafts) {
            PackagePrice packagePrice = packageMap.get(draft.getPackagePriceId());
            if (packagePrice == null) {
                throw new RuntimeException("Không tìm thấy thông tin giá cho gói cước của xe " + draft.getVehicleNo());
            }

            itemPrices.add(packagePrice.getPrice());
            totalAmount = totalAmount.add(packagePrice.getPrice());

            draft.setStatus(BookingStatus.PENDING_PAYMENT);
        }

        validDrafts = bookingDetailRepository.saveAll(validDrafts);

        PaymentInitiateResponse initiateResponse = createPaymentAndGenerateQr(validDrafts, itemPrices, totalAmount,"SEPAY"); // Default gateway selection

        return PaymentCheckoutResponse.builder()
                .paymentId(initiateResponse.getPaymentId())
                .amount(initiateResponse.getAmount())
                .checkoutUrl(initiateResponse.getCheckoutUrl())
                .paymentCode(initiateResponse.getPaymentCode())
                .message(initiateResponse.getMessage())
                .build();
    }

    private PaymentInitiateResponse createPaymentAndGenerateQr(
            List<BookingDetail> validBookings,
            List<BigDecimal> itemPrices,
            BigDecimal totalAmount,
            String gatewayString) {

        Payment payment = Payment.builder()
                .payCode(generatePayCode())
                .gateway(gatewayString)
                .method(PaymentMethod.BANK_TRANSFER)
                .amount(totalAmount)
                .status(Status.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        List<PaymentDetail> paymentDetails = new ArrayList<>();
        for (int i = 0; i < validBookings.size(); i++) {
            BookingDetail booking = validBookings.get(i);
            PaymentDetail detail = PaymentDetail.builder()
                    .paymentId(payment.getId())
                    .bookingDetail(booking)
                    .itemAmount(itemPrices.get(i))
                    .appliedStartDate(booking.getStartDate())
                    .appliedEndDate(booking.getEndDate())
                    .build();
            paymentDetails.add(detail);
        }
        paymentDetailRepository.saveAll(paymentDetails);

        validBookings.forEach(b -> b.setStatus(BookingStatus.PENDING_PAYMENT));
        bookingDetailRepository.saveAll(validBookings);

        PaymentGatewayService selectedGateway = gatewayServices.get(gatewayString);

        if (selectedGateway == null) {
            throw new BusinessException("Hệ thống chưa hỗ trợ cổng thanh toán: " + gatewayString);
        }

        String checkoutUrl = selectedGateway.generateCheckoutUrl(payment.getAmount(), payment.getPayCode());

            log.info("Đã tạo lệnh thanh toán {} thành công, số tiền: {}", payment.getPayCode(), totalAmount);

        return PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .checkoutUrl(checkoutUrl)
                .paymentCode(payment.getPayCode())
                .message("Lên đơn thành công! Vui lòng quét mã QR để thanh toán trong vòng 15 phút.")
                .build();
    }
}

