package com.smartparking.operation.service.admin;

import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.BookingDetailCreateRequest;
import com.smartparking.operation.entity.Booking;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.BookingRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    // Thêm các repo của Group, Package để validate nếu cần

    public Page<BookingDetailDto> getAllBookingDetail(Integer groupId, Integer packageId, Pageable pageable) {
        // Trả thẳng DTO từ câu Query JOIN
        return bookingDetailRepo.findListDto(pageable);
    }

    public BookingDetailDto getBookingDetailById(Integer id) {
        // Thực tế nếu Admin ấn vào xem chi tiết, ông có thể viết 1 câu Query JOIN y hệt
        // trên
        // nhưng trả về 1 Object thay vì Page. Hoặc tái sử dụng hàm trên với
        // PageRequest.of(0, 1)
        return bookingDetailRepo.findDtoById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không thể lấy dữ liệu sau khi cập nhật!"));
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
                .createdAt(request.getCreatedAt())
                .build();

        newBookingDetail = bookingDetailRepo.saveAndFlush(newBookingDetail);

        return bookingDetailRepo.findDtoById(newBookingDetail.getId())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không thể lấy dữ liệu sau khi tạo!"));
    }

    @Transactional
    public BookingDetailDto updateBookingDetail(Integer id, BookingDetailCreateRequest request) {
        // 1. Tìm bản ghi hiện tại
        BookingDetail existing = bookingDetailRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chi tiết hợp đồng (Booking Detail) này!"));

        existing.setBooking(bookingRepo.findById(request.getBookingId()).orElse(null));
        existing.setCustomer(customerRepo.findById(request.getCustomerId()).orElse(null));
        existing.setPackagePriceId(request.getPackagePriceId());
        existing.setVehicleNo(request.getVehicleNo());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setStatus(request.getStatus());

        bookingDetailRepo.saveAndFlush(existing);

        return bookingDetailRepo.findDtoById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không thể lấy dữ liệu sau khi cập nhật!"));
    }

    @Transactional
    public void deleteBookingDetail(Integer id) {
        BookingDetail existing = bookingDetailRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng này!"));

        bookingDetailRepo.delete(existing);
    }

    @Transactional
    public BookingDetailDto createBookingDetailDraft(Integer customerId, Integer groupId,
            BookingDetailCreateRequest request) {
        LocalDateTime expectedStartDate = request.getStartDate();
        if (expectedStartDate == null) {
            throw new RuntimeException("Lỗi: Vui lòng chọn ngày bắt đầu kích hoạt gói cước!");
        }

        expectedStartDate = expectedStartDate.toLocalDate().atStartOfDay();

        if (expectedStartDate.isAfter(LocalDate.now().atStartOfDay())) {
            throw new RuntimeException("Lỗi: Ngày kích hoạt gói cước không hợp lệ (Không được chọn ngày tương lai)");
        }

        GroupsCustomer group = groupsCustomersRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Group (Bãi đỗ)!"));
        if (Boolean.TRUE.equals(group.getIsSynchronize())) {
            if (expectedStartDate.getDayOfMonth() != 1) {
                throw new RuntimeException(
                        "Bãi đỗ này yêu cầu ngày bắt đầu kích hoạt gói cước bắt buộc phải là ngày mùng 1 hàng tháng!");
            }
        }

        Booking booking = bookingRepo.findByGroupId(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng cho Group này!"));

        boolean isVehicleInUse = bookingDetailRepo.existsByVehicleNoAndStatusNotIn(
                request.getVehicleNo(),
                Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED, BookingStatus.COMPLETE));
        if (isVehicleInUse) {
            throw new RuntimeException("Biển số xe " + request.getVehicleNo()
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng!"));
        if (!Objects.equals(customer.getGroupId(), groupId)) {
            throw new RuntimeException("Không có quyền thêm customer nàyu");
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
                .orElseThrow(() -> new RuntimeException("Gói cước không tồn tại!"));

        if (Boolean.FALSE.equals(packagePrice.getIsActive())) {
            throw new RuntimeException("Gói cước này hiện đã ngừng cung cấp!");
        }

        Integer pkgVehTypeId = packagePrice.getPkgVehTypeId();
        PackageVehicleType packageVehicleType = packageVehicleTypeRepository.findById(pkgVehTypeId)
                .orElseThrow(() -> new RuntimeException("Dữ liệu gói cước bị lỗi: Không tìm thấy loại xe áp dụng!"));

        if (!packageVehicleType.getVehicleTypeId().equals(requestedVehicleTypeId)) {
            throw new RuntimeException("Gói cước được chọn không áp dụng cho loại phương tiện này!");
        }

        Integer packageId = packageVehicleType.getPackageId();
        Package parkingPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Dữ liệu gói cước bị lỗi: Không tìm thấy Gói cước gốc!"));

        Integer packageProfileId = parkingPackage.getProfileId();

        GroupsCustomer group = groupsCustomersRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bãi đỗ (Group) này!"));

        if (!group.getProfileId().equals(packageProfileId)) {
            throw new RuntimeException("Gói cước này không được phép áp dụng tại bãi đỗ hiện tại!");
        }

        // đếm số lượng xe của group
        long currentVehicleCount = bookingDetailRepo.countDistinctVehiclesInUse(
                pkgVehTypeId,
                groupId,
                Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED, BookingStatus.COMPLETE));

        if (currentVehicleCount >= packageVehicleType.getMaxQuantity()) {
            throw new RuntimeException("Xin lỗi, số lượng suất đỗ xe cho loại xe này tại bãi đã hết!");
        }
        return packagePrice;
    }

    @Transactional(readOnly = true)
    public List<BookingDetailDto> getDraftBookingDetails(Integer groupId) {
        // 1. Tìm Hợp đồng
        Booking booking = bookingRepo.findByGroupId(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng cho Group này!"));

        return bookingDetailRepo.findDtoByBookingIdAndStatus(booking.getId(), BookingStatus.DRAFT);
    }

    @Transactional
    public void clearAllDrafts(Integer customerId, Integer groupId) {
        // 1. Tìm Hợp đồng
        Booking booking = bookingRepo.findByGroupId(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng cho Group này!"));

        // 2. Lấy danh sách các Entity đang nằm trong giỏ
        List<BookingDetail> draftDetails = bookingDetailRepo.findByBookingIdAndStatus(booking.getId(),
                BookingStatus.DRAFT);

        if (draftDetails.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống!");
        }

        // 3. Xóa mềm: Đổi trạng thái sang CANCELED
        draftDetails.forEach(detail -> detail.setStatus(BookingStatus.CANCELED));

        // 4. Lưu lại toàn bộ vào DB
        bookingDetailRepo.saveAll(draftDetails);

        log.info("Khách hàng {} đã hủy toàn bộ giỏ hàng thuộc Hợp đồng {}", customerId, booking.getId());
    }

}
