package com.smartparking.payment.service;

import com.smartparking.operation.entity.Booking;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.entity.ParkingSession;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.ParkingSessionRepository;
import com.smartparking.operation.specification.BookingDetailSpecs;
import com.smartparking.payment.dto.BillingResult;
import com.smartparking.payment.dto.request.CheckoutRequest;
import com.smartparking.payment.dto.request.payment.PaymentBookingRequest;
import com.smartparking.payment.dto.request.payment.PaymentFilterRequest;
import com.smartparking.payment.dto.request.payment.PaymentSessionRequest;
import com.smartparking.payment.dto.request.payment.RenewItemRequest;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final VietQRService vietQRService;


    @Value("${app.sepay.bank-account}")
    private String bankAccount;

    @Value("${app.sepay.bank-name}")
    private String bankName;

    @Value("${app.sepay.account-name}")
    private String accountName;


    // 1. GET LIST CỦA TÔI
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch hoặc bạn không có quyền truy cập!"));

        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(paymentId);

        return PaymentTreeResponse.builder()
                .paymentInfo(mapToPaymentResponse(payment))
                .details(details.stream().map(this::mapToPaymentDetailResponse).collect(Collectors.toList()))
                .build();
    }


    @Transactional
    public void cancelPayment(Long paymentId, List<Integer> myGroupIds) {

        // 1 & 2. TÌM KIẾM VÀ CHỐT CHẶN BẢO MẬT (IDOR) BẰNG SPECIFICATION
        // Gộp luôn điều kiện belongsToGroupIds và check ID vào 1 câu query
        Specification<Payment> spec = Specification
                .where(PaymentSpecs.belongsToGroupIds(myGroupIds))
                .and((root, query, cb) -> cb.equal(root.get("id"), paymentId));

        // Dùng findOne thay vì findById
        Payment payment = paymentRepository.findOne(spec)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại hoặc bạn không có quyền truy cập!"));

        // 3. CHỐT CHẶN NGHIỆP VỤ: Chỉ được hủy đơn PENDING
        if (payment.getStatus() != Status.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy giao dịch đang chờ thanh toán!");
        }

        // 4. THỰC THI HỦY PAYMENT
        payment.setStatus(Status.CANCELED);
        paymentRepository.save(payment);

        // 5. HỦY CÁC VÉ ẢO (DRAFT BOOKING) ĐỂ GIẢI PHÓNG XE
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(paymentId);
        if (!details.isEmpty()) {
            List<Integer> draftBookingIds = details.stream()
                    .map(detail -> detail.getBookingDetail().getId())
                    .toList();

            // Nhớ đảm bảo trong BookingDetailRepository ông đã viết hàm này bằng @Modifying và @Query nhé
            bookingDetailRepository.updateStatusToCanceled(draftBookingIds);
        }
    }


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
    public PaymentInitiateResponse initiateBookingPayment(PaymentBookingRequest request, List<Integer> myGroupIds) {

        //1 lấy list các booking_detail để gia hạn này.
        List<Integer> oldBookingDetailIds = request.getItems().stream()
                .map(RenewItemRequest::getOldBookingDetailId)
                .toList();

        //2 nhét  các booking_detail vào để kiểm tra bảo mật
        List<BookingDetail> oldBookingDetail = validateAndGetOldBookings(oldBookingDetailIds, myGroupIds);

        //3 xem có đồng bộ không
        Boolean isSynchronize = oldBookingDetail.getFirst().getBooking().getGroup().getIsSynchronize();
        if (isSynchronize == null) isSynchronize = false;

        //4 lấy hết các packagePriceId mà các booking_detail mới muốn dùng
        List<Integer> packagePriceIds = request.getItems().stream()
                .map(RenewItemRequest::getNewPackagePriceId)
                .distinct()
                .toList();
        //5 tìm các packageprice theo list id trên
        List<PackagePrice> packagePrices = packagePriceRepository.findAllById(packagePriceIds);
        if (packagePrices.size() != packagePriceIds.size()) {
            throw new RuntimeException("Có lỗi xảy ra: Một hoặc nhiều Gói cước (PackagePrice) bạn chọn không tồn tại trong hệ thống!");
        }
        //6 map packagePrice và packagePriceId vào cho dễ tìm
        Map<Integer, PackagePrice> packageMap = packagePrices.stream()
                .collect(Collectors.toMap(PackagePrice::getId, pp -> pp));

        // 6 tạo payment
// 6. TẠO CÁC BIẾN LƯU TRỮ TRUNG GIAN
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingDetail> draftBookings = new ArrayList<>();
        List<BigDecimal> calculatedPrices = new ArrayList<>();

        // Lấy tổng các biển số và map dữ liệu Max EndDate (như cũ)
        List<String> vehicleNos = oldBookingDetail.stream()
                .map(BookingDetail::getVehicleNo)
                .toList();

        List<Object[]> maxDatesRaw = bookingDetailRepository.findMaxEndDatesByVehicleNos(vehicleNos, BookingStatus.ACTIVE);
        Map<String, LocalDateTime> maxEndDateMap = maxDatesRaw.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (LocalDateTime) row[1]
                ));

        Booking booking = oldBookingDetail.getFirst().getBooking();

        // 7. VÒNG LẶP TÍNH TIỀN VÀ TẠO VÉ NHÁP (Chưa lưu DB hóa đơn)
        for (RenewItemRequest item : request.getItems()) {
            BookingDetail oldBooking = oldBookingDetail.stream()
                    .filter(b -> b.getId().equals(item.getOldBookingDetailId()))
                    .findFirst().orElseThrow();

            PackagePrice selectedPackage = packageMap.get(item.getNewPackagePriceId());
            LocalDateTime maxEndDateFromDb = maxEndDateMap.get(oldBooking.getVehicleNo());

            BillingResult billing = calculateBilling(maxEndDateFromDb, selectedPackage, isSynchronize);

            // Cộng dồn tiền vào tổng
            totalAmount = totalAmount.add(billing.getFinalPrice());
            calculatedPrices.add(billing.getFinalPrice());

            // Sinh vé nháp
            BookingDetail draftBooking = BookingDetail.builder()
                    .booking(booking)
                    .customer(oldBooking.getCustomer())
                    .packagePriceId(selectedPackage.getId())
                    .vehicleNo(oldBooking.getVehicleNo())
                    .startDate(billing.getStartDate())
                    .endDate(billing.getEndDate())
                    .status(BookingStatus.PENDING_PAYMENT)
                    .build();
            draftBookings.add(draftBooking);
        }

        // 8. LƯU VÉ NHÁP XUỐNG DB TRƯỚC
        draftBookings = bookingDetailRepository.saveAll(draftBookings);

        // =========================================================
        // 9. BÂY GIỜ MỚI ĐƯỢC TẠO VÀ LƯU PAYMENT (VÌ ĐÃ CÓ TỔNG TIỀN)
        // =========================================================
        String payCode = generatePayCode();
        PaymentMethod method = determineMethodFromGateway(request.getGateway().toString());
        Payment payment = Payment.builder()
                .payCode(payCode)
                .gateway(request.getGateway())
                .method(method)
                .amount(totalAmount) // Nhét luôn tiền vào đây!
                .status(Status.PENDING)
                .build();

        // Lệnh lưu an toàn 100% vì đã đủ Not Null Constraint
        payment = paymentRepository.save(payment);

        // 10. TẠO VÀ LƯU PAYMENT DETAIL (Đã có ID của payment vừa lưu)
        List<PaymentDetail> paymentDetails = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            BookingDetail currentDraft = draftBookings.get(i);

            PaymentDetail detail = PaymentDetail.builder()
                    .paymentId(payment.getId()) // Lấy ID an toàn
                    .bookingDetail(draftBookings.get(i))
                    .itemAmount(calculatedPrices.get(i))
                    .appliedStartDate(currentDraft.getStartDate())
                    .appliedEndDate(currentDraft.getEndDate())
                    .build();
            paymentDetails.add(detail);
        }
        paymentDetailRepository.saveAll(paymentDetails);

        // 11. TRẢ VỀ KẾT QUẢ
        String realQrUrl = vietQRService.generateQrUrl(payment.getAmount(), payment.getPayCode());
        return PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .checkoutUrl(realQrUrl)
                .paymentCode(payment.getPayCode())
                .message("Lên đơn thành công! Vui lòng quét mã QR để thanh toán trong vòng 15 phút.")
                .build();
    }

    private BillingResult calculateBilling(LocalDateTime referenceEndDate, PackagePrice packagePrice, boolean isSynchronize) {
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
            int remainingDays = daysInCurrentMonth - now.getDayOfMonth() + 1; // Bao gồm cả ngày hôm nay

            BigDecimal partialMonthPrice = pricePerMonth
                    .multiply(BigDecimal.valueOf(remainingDays))
                    .divide(BigDecimal.valueOf(daysInCurrentMonth), 0, RoundingMode.DOWN); // Làm tròn xuống không lấy lẻ

            BigDecimal fullMonthsPrice = pricePerMonth.multiply(BigDecimal.valueOf(duration - 1))
                    .setScale(0, RoundingMode.DOWN);

            BigDecimal finalPrice = partialMonthPrice.add(fullMonthsPrice);

            return new BillingResult(newStartDate, newEndDate, finalPrice);
        }
    }

    private List<BookingDetail> validateAndGetOldBookings(List<Integer> oldBookingDetailIds, List<Integer> myGroupIds) {

        // 1. Lấy danh sách từ Database
        List<BookingDetail> oldBookingDetail = bookingDetailRepository.findAllById(oldBookingDetailIds);
        if (oldBookingDetail.isEmpty() || oldBookingDetail.size() != oldBookingDetailIds.size()) {
            throw new RuntimeException("Không tìm thấy hợp đồng cũ, hoặc có ID không hợp lệ!");
        }

        // 2. KIỂM TRA: Có cùng thuộc 1 Booking không?
        Integer firstBookingId = oldBookingDetail.getFirst().getBooking().getId();
        boolean isSameBooking = oldBookingDetail.stream()
                .allMatch(bd -> bd.getBooking().getId().equals(firstBookingId));

        if (!isSameBooking) {
            throw new RuntimeException("Các xe được chọn thanh toán phải thuộc cùng một nhóm hợp đồng!");
        }

        // 3. KIỂM TRA BẢO MẬT (IDOR Protection): Hợp đồng này có thuộc Group của User không?
        Integer bookingGroupId = oldBookingDetail.getFirst().getBooking().getGroupId();
        if (myGroupIds == null || !myGroupIds.contains(bookingGroupId)) {
            throw new RuntimeException("Truy cập bị từ chối! Bạn không có quyền thanh toán cho hợp đồng này.");
        }

        // 4. KIỂM TRA KHÓA (LOCK CHECK): Xem xe có đang bị kẹt ở giao dịch PENDING hoặc PENDING_ACTIVE không
        List<String> licensePlates = oldBookingDetail.stream()
                .map(BookingDetail::getVehicleNo)
                .toList();

        // Dùng Specification lồng nhau để tạo điều kiện: (Trạng thái = PENDING_PAYMENT HOẶC PENDING_ACTIVE)
        Specification<BookingDetail> lockSpec = Specification
                .where(BookingDetailSpecs.hasVehicleNoIn(licensePlates))
                .and(
                        Specification.where(BookingDetailSpecs.hasStatus(BookingStatus.PENDING_PAYMENT))
                                .or(BookingDetailSpecs.hasStatus(BookingStatus.PENDING_ACTIVATION))
                );

        boolean isLocked = bookingDetailRepository.exists(lockSpec);

        if (isLocked) {
            throw new RuntimeException("Một hoặc nhiều xe đang có giao dịch chờ thanh toán hoặc vé đang chờ kích hoạt. Vui lòng hoàn tất giao dịch cũ hoặc chờ vé được kích hoạt!");
        }
        return oldBookingDetail;
    }

    private PaymentMethod determineMethodFromGateway(String gateway) {
        if (gateway == null) return PaymentMethod.CASH;

        return switch (gateway.toUpperCase()) {
            case "SEPAY", "PAYOS" -> PaymentMethod.BANK_TRANSFER;
            case "VNPAY" -> PaymentMethod.BANK_TRANSFER; // VNPAY có thể mở rộng sau
            case "CASH", "MANUAL" -> PaymentMethod.CASH;
            default -> PaymentMethod.BANK_TRANSFER; // Mặc định
        };
    }

    @Transactional
    public PaymentCheckoutResponse checkoutBookingDetails(Integer customerId, CheckoutRequest request) {

        List<Integer> detailIds = request.getBookingDetailIds();
        if (detailIds == null || detailIds.isEmpty()) {
            throw new RuntimeException("Danh sách thanh toán không được để trống!");
        }

        // 1. KÉO DANH SÁCH GIỎ HÀNG TỪ DB LÊN
        List<BookingDetail> drafts = bookingDetailRepository.findAllById(detailIds);

        if (drafts.size() != detailIds.size()) {
            throw new RuntimeException("Một số gói đăng ký không tồn tại hoặc đã bị xóa khỏi hệ thống!");
        }

        List<BookingDetail> expiredDrafts = new ArrayList<>();
        List<BookingDetail> validDrafts = new ArrayList<>();
        LocalDateTime now = LocalDate.now().atStartOfDay();

        for (BookingDetail draft : drafts) {
            // Chốt chặn bảo mật 1 & 2
            if (!draft.getCustomer().getId().equals(customerId)) {
                throw new RuntimeException("Phát hiện gói đăng ký không hợp lệ. Giao dịch bị hủy!");
            }
            if (draft.getStatus() != BookingStatus.DRAFT) {
                throw new RuntimeException("Gói đăng ký cho xe " + draft.getVehicleNo() + " đã thay đổi trạng thái. Vui lòng kiểm tra lại giỏ hàng!");
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
        }

        // Chốt chặn cuối: Nếu quét xong mà giỏ hàng toàn đồ hết hạn thì ném lỗi dừng luôn
        if (validDrafts.isEmpty()) {
            throw new RuntimeException("Tất cả các gói cước được chọn đã trễ ngày kích hoạt (Quá hạn). Vui lòng dọn giỏ hàng và tạo đăng ký mới!");
        }


        // 2. KÉO BẢNG GIÁ (PACKAGE PRICE) ĐỂ TÍNH TIỀN (Chống N+1 Query)
        List<Integer> packageIds = drafts.stream()
                .map(BookingDetail::getPackagePriceId)
                .distinct()
                .toList();

        Map<Integer, PackagePrice> packageMap = packagePriceRepository.findAllById(packageIds).stream()
                .collect(Collectors.toMap(PackagePrice::getId, pp -> pp));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PaymentDetail> paymentDetails = new ArrayList<>();

        // 3. QUÉT BẢO MẬT & TÍNH TỔNG TIỀN
        for (BookingDetail draft : drafts) {

            // Chốt chặn 1: Xe này có đúng của ông khách đang đăng nhập không?
            if (!draft.getCustomer().getId().equals(customerId)) {
                throw new RuntimeException("Phát hiện gói đăng ký không hợp lệ. Giao dịch bị hủy!");
            }

            // Chốt chặn 2: Trạng thái có chuẩn là DRAFT (Giỏ hàng) không?
            if (draft.getStatus() != BookingStatus.DRAFT) {
                throw new RuntimeException("Gói đăng ký cho xe " + draft.getVehicleNo() + " đã thay đổi trạng thái hoặc hết hạn. Vui lòng kiểm tra lại giỏ hàng!");
            }

            // Lấy giá tiền và cộng dồn
            PackagePrice packagePrice = packageMap.get(draft.getPackagePriceId());
            if (packagePrice == null) {
                throw new RuntimeException("Không tìm thấy thông tin giá cho gói cước của xe " + draft.getVehicleNo());
            }
            totalAmount = totalAmount.add(packagePrice.getPrice());

            // Đổi trạng thái BookingDetail sang chờ thanh toán
            draft.setStatus(BookingStatus.PENDING_PAYMENT);

            // Khởi tạo Chi tiết hóa đơn (PaymentDetail)
            PaymentDetail pd = new PaymentDetail();
            pd.setBookingDetail(draft);
            pd.setItemAmount(packagePrice.getPrice()); // Chốt cứng giá tiền tại thời điểm mua
            pd.setAppliedStartDate(draft.getStartDate());
            pd.setAppliedEndDate(draft.getEndDate());
            paymentDetails.add(pd);
        }

        // 4. TẠO HÓA ĐƠN GỐC (PAYMENT)
        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setStatus(Status.PENDING);
        payment.setGateway("SEPAY");
        payment.setMethod(PaymentMethod.BANK_TRANSFER);
        payment.setPayCode(generatePayCode());
        Payment savedPayment = paymentRepository.save(payment);

        // 5. GẮN KẾT VÀ LƯU CHI TIẾT HÓA ĐƠN
        for (PaymentDetail pd : paymentDetails) {
            pd.setPaymentId(savedPayment.getId());
        }
        paymentDetailRepository.saveAll(paymentDetails);
        bookingDetailRepository.saveAll(drafts);

        // 6. TẠO LINK THANH TOÁN (MOCK)
        String realQrUrl = vietQRService.generateQrUrl(savedPayment.getAmount(), savedPayment.getPayCode());
        return PaymentCheckoutResponse.builder()
                .paymentId(savedPayment.getId())
                .amount(savedPayment.getAmount())
                .checkoutUrl(realQrUrl)
                .paymentCode(savedPayment.getPayCode())
                .message("Lên đơn thành công! Vui lòng quét mã QR để thanh toán trong vòng 15 phút.")
                .build();
    }
}

