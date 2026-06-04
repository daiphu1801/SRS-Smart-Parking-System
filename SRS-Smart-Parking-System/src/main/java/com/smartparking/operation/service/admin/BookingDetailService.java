package com.smartparking.operation.service.admin;

import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.BookingDetailCreateRequest;
import com.smartparking.operation.dto.request.RenewItemRequest;
import com.smartparking.operation.dto.request.RenewalBookingRequest;
import com.smartparking.operation.entity.Booking;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.BookingRepository;
import com.smartparking.operation.specification.BookingDetailSpecs;
import com.smartparking.payment.dto.BillingResult;
import com.smartparking.payment.service.CustomerPaymentService;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.entity.Package;
import com.smartparking.subscription.entity.PackageVehicleType;
import com.smartparking.subscription.repository.PackagePriceRepository;
import com.smartparking.subscription.repository.PackageRepository;
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
public class BookingDetailService {

        private final BookingDetailRepository bookingDetailRepo;
        private final BookingRepository bookingRepo;
        private final CustomerRepository customerRepo;
        private final PackagePriceRepository packagePriceRepository;
        private final PackageVehicleTypeRepository packageVehicleTypeRepository;
        private final PackageRepository packageRepository;
        private final GroupsCustomersRepository groupsCustomersRepository;
        private final CustomerPaymentService customerPaymentService;
        // Add additional repositories for validation if necessary

        public Page<BookingDetailDto> getAllBookingDetail(Integer groupId, Integer packageId, Pageable pageable) {
                // Return DTO directly from the JOIN query
                return bookingDetailRepo.findListDto(pageable);
        }

        public BookingDetailDto getBookingDetailById(Integer id) {
                // Retrieve a single object directly via JOIN instead of using PageRequest
                return bookingDetailRepo.findDtoById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Lỗi: Không thể lấy dữ liệu sau khi cập nhật!"));
        }

        @Transactional
        public BookingDetailDto createBookingDetail(BookingDetailCreateRequest request) {

                BookingDetail newBookingDetail = BookingDetail.builder()
                                .customer(customerRepo.findById(request.getCustomerId()).orElse(null))
                                .booking(bookingRepo.findById(request.getBookingId()).orElse(null))
                                .packagePriceId(request.getPackagePriceId())
                                .vehicleNo(request.getVehicleNo())
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .status(request.getStatus())
                                .build();

                newBookingDetail = bookingDetailRepo.saveAndFlush(newBookingDetail);

                return bookingDetailRepo.findDtoById(newBookingDetail.getId())
                                .orElseThrow(() -> new RuntimeException("Lỗi: Không thể lấy dữ liệu sau khi tạo!"));
        }

        @Transactional
        public BookingDetailDto updateBookingDetail(Integer id, BookingDetailCreateRequest request) {
                // Find the existing record
                BookingDetail existing = bookingDetailRepo.findById(id)
                                .orElseThrow(() -> new BusinessException(
                                                "Không tìm thấy Chi tiết hợp đồng (Booking Detail) này!"));

                existing.setBooking(bookingRepo.findById(request.getBookingId()).orElse(null));
                existing.setCustomer(customerRepo.findById(request.getCustomerId()).orElse(null));
                existing.setPackagePriceId(request.getPackagePriceId());
                existing.setVehicleNo(request.getVehicleNo());
                existing.setStartDate(request.getStartDate());
                existing.setEndDate(request.getEndDate());
                existing.setStatus(request.getStatus());

                bookingDetailRepo.saveAndFlush(existing);

                return bookingDetailRepo.findDtoById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Lỗi: Không thể lấy dữ liệu sau khi cập nhật!"));
        }

        @Transactional
        public void deleteBookingDetail(Integer id) {
                BookingDetail existing = bookingDetailRepo.findById(id)
                                .orElseThrow(() -> new BusinessException("Không tìm thấy Hợp đồng này!"));

                bookingDetailRepo.delete(existing);
        }

        @Transactional
        public BookingDetailDto createBookingDetailDraft(Integer customerId, Integer groupId,
                        BookingDetailCreateRequest request) {
                LocalDateTime expectedStartDate = request.getStartDate();
                if (expectedStartDate == null) {
                        throw new BusinessException("Lỗi: Vui lòng chọn ngày bắt đầu kích hoạt gói cước!");
                }

                expectedStartDate = expectedStartDate.toLocalDate().atStartOfDay();

                if (expectedStartDate.isAfter(LocalDate.now().atStartOfDay())) {
                        throw new BusinessException(
                                        "Lỗi: Ngày kích hoạt gói cước không hợp lệ (Không được chọn ngày tương lai)");
                }

                GroupsCustomer group = groupsCustomersRepository.findById(groupId)
                                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin Group (Bãi đỗ)!"));
                if (Boolean.TRUE.equals(group.getIsSynchronize())) {
                        if (expectedStartDate.getDayOfMonth() != 1) {
                                throw new BusinessException(
                                                "Bãi đỗ này yêu cầu ngày bắt đầu kích hoạt gói cước bắt buộc phải là ngày mùng 1 hàng tháng!");
                        }
                }

                Booking booking = bookingRepo.findByGroupId(groupId)
                                .orElseThrow(() -> new BusinessException("Không tìm thấy Hợp đồng cho Group này!"));

                boolean isVehicleInUse = bookingDetailRepo.existsByVehicleNoAndStatusNotIn(
                                request.getVehicleNo(),
                                Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED, BookingStatus.COMPLETE));
                if (isVehicleInUse) {
                        throw new BusinessException("Biển số xe " + request.getVehicleNo()
                                        + " đã được đăng ký hoặc đang nằm trong giỏ hàng chờ thanh toán!");
                }

                PackagePrice packagePrice = validateAndGetPackagePrice(
                                request.getPackagePriceId(),
                                request.getVehicleTypeId(),
                                groupId);

                Integer durationMonths = packagePrice.getDurationMonths();

                BookingDetail draftDetail = new BookingDetail();

                draftDetail.setBooking(booking);
                Customer customer = customerRepo.findById(customerId)
                                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin khách hàng!"));
                if (!Objects.equals(customer.getGroupId(), groupId)) {
                        throw new BusinessException("Không có quyền thêm customer nàyu");
                }

                draftDetail.setCustomer(customer);
                draftDetail.setPackagePriceId(packagePrice.getId());
                draftDetail.setVehicleNo(request.getVehicleNo());
                draftDetail.setStartDate(expectedStartDate);
                draftDetail.setEndDate(expectedStartDate.plusMonths(durationMonths).minusSeconds(1));
                draftDetail.setStatus(BookingStatus.DRAFT);

                BookingDetail savedDetail = bookingDetailRepo.save(draftDetail);

                log.info("Khách hàng {} vừa thêm xe {} vào giỏ hàng (Booking {})",
                                customerId, savedDetail.getVehicleNo(), booking.getId());

                return BookingDetailDto.builder()
                                .id(savedDetail.getId())
                                .bookingId(booking.getId())
                                .customerId(customer.getId())
                                .packagePriceId(savedDetail.getPackagePriceId())
                                .vehicleNo(savedDetail.getVehicleNo())
                                .startDate(savedDetail.getStartDate())
                                .endDate(savedDetail.getEndDate())
                                .status(savedDetail.getStatus())
                                .createdAt(savedDetail.getCreatedAt())
                                .packagePriceName(packagePrice.getPackagePriceName())
                                .price(packagePrice.getPrice())
                                .build();
        }

        private PackagePrice validateAndGetPackagePrice(Integer packagePriceId, Integer requestedVehicleTypeId,
                        Integer groupId) {

                PackagePrice packagePrice = packagePriceRepository.findById(packagePriceId)
                                .orElseThrow(() -> new BusinessException("Gói cước không tồn tại!"));

                if (Boolean.FALSE.equals(packagePrice.getIsActive())) {
                        throw new BusinessException("Gói cước này hiện đã ngừng cung cấp!");
                }

                Integer pkgVehTypeId = packagePrice.getPkgVehTypeId();
                PackageVehicleType packageVehicleType = packageVehicleTypeRepository.findByIdWithLock(pkgVehTypeId)
                        .orElseThrow(() -> new BusinessException("Dữ liệu gói cước bị lỗi: Không tìm thấy loại xe áp dụng!"));

                if (!packageVehicleType.getVehicleTypeId().equals(requestedVehicleTypeId)) {
                        throw new BusinessException("Gói cước được chọn không áp dụng cho loại phương tiện này!");
                }

                Integer packageId = packageVehicleType.getPackageId();
                Package parkingPackage = packageRepository.findById(packageId)
                                .orElseThrow(() -> new BusinessException(
                                                "Dữ liệu gói cước bị lỗi: Không tìm thấy Gói cước gốc!"));

                Integer packageProfileId = parkingPackage.getProfileId();

                GroupsCustomer group = groupsCustomersRepository.findById(groupId)
                                .orElseThrow(() -> new BusinessException("Không tìm thấy bãi đỗ (Group) này!"));

                if (!group.getProfileId().equals(packageProfileId)) {
                        throw new BusinessException("Gói cước này không được phép áp dụng tại bãi đỗ hiện tại!");
                }

                // Count active vehicles in the group to enforce capacity limits
                long currentVehicleCount = bookingDetailRepo.countDistinctVehiclesInUse(
                                pkgVehTypeId,
                                groupId,
                                Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED, BookingStatus.COMPLETE));

                if (currentVehicleCount >= packageVehicleType.getMaxQuantity()) {
                        throw new BusinessException("Xin lỗi, số lượng suất đỗ xe cho loại xe này tại bãi đã hết!");
                }
                return packagePrice;
        }

        @Transactional()
        public List<BookingDetailDto> getDraftBookingDetails(Integer groupId) {
                // Retrieve the parent booking
                Booking booking = bookingRepo.findByGroupId(groupId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng cho Group này!"));
                List<BookingDetailDto> allDrafts = bookingDetailRepo.findDtoByBookingIdAndStatus(booking.getId(), BookingStatus.DRAFT);

                LocalDateTime todayStart = LocalDate.now().atStartOfDay();

                List<Integer> idsToCancel = allDrafts.stream()
                        .filter(dto -> dto.getStartDate() != null && dto.getStartDate().isBefore(todayStart))
                        .map(BookingDetailDto::getId)
                        .toList();

                if (!idsToCancel.isEmpty()) {
                        List<BookingDetail> entitiesToCancel = bookingDetailRepo.findAllById(idsToCancel);
                        entitiesToCancel.forEach(detail -> detail.setStatus(BookingStatus.CANCELED));
                        bookingDetailRepo.saveAll(entitiesToCancel); // Persist updates to the database

                        log.info("Hệ thống tự động hủy {} bản nháp quá hạn thuộc Hợp đồng {}", idsToCancel.size(), booking.getId());
                }

                return allDrafts.stream()
                        .filter(dto -> !idsToCancel.contains(dto.getId()))
                        .toList();
        }

        @Transactional
        public void deleteSelectedDrafts(Integer customerId, Integer groupId, List<Integer> draftIds) {

                if (draftIds == null || draftIds.isEmpty()) {
                        throw new BusinessException("Danh sách xe cần xóa trống!");
                }

                Booking booking = bookingRepo.findByGroupId(groupId)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy Hợp đồng cho Group này!"));
                List<BookingDetail> draftsToDelete = bookingDetailRepo.findAllById(draftIds);

                if (draftsToDelete.size() != draftIds.size()) {
                        throw new BusinessException("Có lỗi xảy ra: Dữ liệu không đồng bộ, một số xe không tồn tại!");
                }

                for (BookingDetail detail : draftsToDelete) {

                        if (!detail.getBooking().getId().equals(booking.getId())) {
                                throw new BusinessException("Truy cập bị từ chối: Xe mang biển số " + detail.getVehicleNo() + " không thuộc bãi đỗ này!");
                        }

                        if (!detail.getCustomer().getId().equals(customerId)) {
                                throw new BusinessException("Truy cập bị từ chối: Bạn không có quyền xóa xe của khách hàng khác!");
                        }

                        if (detail.getStatus() != BookingStatus.DRAFT) {
                                throw new BusinessException("Lỗi: Xe " + detail.getVehicleNo() + " không ở trạng thái Bản nháp (Giỏ hàng) nên không thể xóa!");
                        }
                }

                draftsToDelete.forEach(detail -> detail.setStatus(BookingStatus.CANCELED));

                bookingDetailRepo.saveAll(draftsToDelete);

                log.info("Khách hàng {} đã hủy {} xe trong giỏ hàng thuộc Hợp đồng {}",
                        customerId, draftsToDelete.size(), booking.getId());
        }

        @Transactional
        public List<BookingDetailDto> createRenewalDrafts(RenewalBookingRequest request, List<Integer> myGroupIds) {

                // Retrieve legacy booking details for security and expiration validation
                List<Integer> oldBookingDetailIds = request.getItems().stream()
                        .map(RenewItemRequest::getOldBookingDetailId)
                        .toList();
                List<BookingDetail> oldBookingDetails = validateAndGetOldBookings(oldBookingDetailIds, myGroupIds);

                // Check synchronization flags
                Booking booking = oldBookingDetails.getFirst().getBooking();
                Boolean isSynchronize = booking.getGroup().getIsSynchronize();
                if (isSynchronize == null) isSynchronize = false;

                // Retrieve requested tariff packages for renewal
                List<Integer> packagePriceIds = request.getItems().stream()
                        .map(RenewItemRequest::getNewPackagePriceId)
                        .distinct()
                        .toList();
                List<PackagePrice> packagePrices = packagePriceRepository.findAllById(packagePriceIds);
                if (packagePrices.size() != packagePriceIds.size()) {
                        throw new BusinessException("Có lỗi xảy ra: Một hoặc nhiều Gói cước bạn chọn không tồn tại!");
                }
                Map<Integer, PackagePrice> packageMap = packagePrices.stream()
                        .collect(Collectors.toMap(PackagePrice::getId, pp -> pp));

                // Calculate continuous extension (Retrieve the furthest expiration date for the vehicle)
                List<String> vehicleNos = oldBookingDetails.stream()
                        .map(BookingDetail::getVehicleNo)
                        .toList();
                List<Object[]> maxDatesRaw = bookingDetailRepo.findMaxEndDatesByVehicleNos(vehicleNos, BookingStatus.ACTIVE);
                Map<String, LocalDateTime> maxEndDateMap = maxDatesRaw.stream()
                        .collect(Collectors.toMap(
                                row -> (String) row[0],
                                row -> (LocalDateTime) row[1]
                        ));

                // LOOP: Generate draft bookings only (No financial mutation or payment creation)
                List<BookingDetail> draftBookings = new ArrayList<>();

                for (RenewItemRequest item : request.getItems()) {
                        BookingDetail oldBooking = oldBookingDetails.stream()
                                .filter(b -> b.getId().equals(item.getOldBookingDetailId()))
                                .findFirst().orElseThrow();

                        PackagePrice selectedPackage = packageMap.get(item.getNewPackagePriceId());
                        LocalDateTime maxEndDateFromDb = maxEndDateMap.get(oldBooking.getVehicleNo());

                        // Calculate continuous startDate and endDate
                        BillingResult billing = customerPaymentService.calculateBilling(maxEndDateFromDb, selectedPackage, isSynchronize);

                        // Create an isolated draft booking
                        BookingDetail draftBooking = new BookingDetail();
                        draftBooking.setBooking(booking);
                        draftBooking.setCustomer(oldBooking.getCustomer());
                        draftBooking.setPackagePriceId(selectedPackage.getId());
                        draftBooking.setVehicleNo(oldBooking.getVehicleNo());
                        draftBooking.setStartDate(billing.getStartDate());
                        draftBooking.setEndDate(billing.getEndDate());
                        draftBooking.setStatus(BookingStatus.DRAFT); // IMPORTANT: Must remain in the cart (DRAFT status)

                        draftBookings.add(draftBooking);
                }

                // Persist all drafts to the database
                List<BookingDetail> savedDrafts = bookingDetailRepo.saveAll(draftBookings);

                log.info("Đã tạo thành công {} bản nháp gia hạn cho Hợp đồng {}", savedDrafts.size(), booking.getId());

                // Map entities to DTOs for the client response
                return savedDrafts.stream().map(saved -> {
                        PackagePrice pp = packageMap.get(saved.getPackagePriceId());
                        return BookingDetailDto.builder()
                                .id(saved.getId())
                                .bookingId(saved.getBooking().getId())
                                .customerId(saved.getCustomer().getId())
                                .packagePriceId(saved.getPackagePriceId())
                                .vehicleNo(saved.getVehicleNo())
                                .startDate(saved.getStartDate())
                                .endDate(saved.getEndDate())
                                .status(saved.getStatus())
                                .createdAt(saved.getCreatedAt())
                                .packagePriceName(pp.getPackagePriceName())
                                .price(pp.getPrice())
                                .build();
                }).toList();
        }

        private List<BookingDetail> validateAndGetOldBookings(List<Integer> oldBookingDetailIds, List<Integer> myGroupIds) {

                // Fetch data from database
                List<BookingDetail> oldBookingDetail = bookingDetailRepo.findAllById(oldBookingDetailIds);
                if (oldBookingDetail.isEmpty() || oldBookingDetail.size() != oldBookingDetailIds.size()) {
                        throw new BusinessException("Không tìm thấy hợp đồng cũ, hoặc có ID không hợp lệ!");
                }

                // VALIDATION: Ensure all items belong to the same booking
                Integer firstBookingId = oldBookingDetail.getFirst().getBooking().getId();
                boolean isSameBooking = oldBookingDetail.stream()
                        .allMatch(bd -> bd.getBooking().getId().equals(firstBookingId));

                if (!isSameBooking) {
                        throw new BusinessException("Các xe được chọn thanh toán phải thuộc cùng một nhóm hợp đồng!");
                }

                boolean hasExpired = oldBookingDetail.stream()
                        .anyMatch(bd -> bd.getStatus() == BookingStatus.EXPIRED
                                || bd.getEndDate().isBefore(LocalDateTime.now()));

                if (hasExpired) {
                        throw new BusinessException("Có xe đã hết hạn hợp đồng! Vui lòng vào mục Thêm Xe để đăng ký lại từ đầu.");
                }

                // SECURITY VALIDATION (IDOR): Ensure the booking belongs to the user's authorized group
                Integer bookingGroupId = oldBookingDetail.getFirst().getBooking().getGroupId();
                if (myGroupIds == null || !myGroupIds.contains(bookingGroupId)) {
                        throw new BusinessException("Truy cập bị từ chối! Bạn không có quyền thanh toán cho hợp đồng này.");
                }

                // CONCURRENCY LOCK CHECK: Ensure vehicle is not tied up in PENDING or PENDING_ACTIVE transactions
                List<String> licensePlates = oldBookingDetail.stream()
                        .map(BookingDetail::getVehicleNo)
                        .toList();

                // Nested Specification for locking conditions (Status = PENDING_PAYMENT OR PENDING_ACTIVATION)
                Specification<BookingDetail> lockSpec = Specification
                        .where(BookingDetailSpecs.hasVehicleNoIn(licensePlates))
                        .and(
                                Specification.where(BookingDetailSpecs.hasStatus(BookingStatus.PENDING_PAYMENT))
                                        .or(BookingDetailSpecs.hasStatus(BookingStatus.PENDING_ACTIVATION))
                        );

                boolean isLocked = bookingDetailRepo.exists(lockSpec);

                if (isLocked) {
                        throw new BusinessException("Một hoặc nhiều xe đang có giao dịch chờ thanh toán hoặc vé đang chờ kích hoạt. Vui lòng hoàn tất giao dịch cũ hoặc chờ vé được kích hoạt!");
                }
                return oldBookingDetail;
        }


}
