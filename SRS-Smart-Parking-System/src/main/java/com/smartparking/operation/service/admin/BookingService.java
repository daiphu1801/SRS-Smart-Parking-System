package com.smartparking.operation.service.admin;

import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.BookingCreateRequest;
import com.smartparking.operation.dto.response.BookingAndDetailResponse;
import com.smartparking.operation.dto.response.BookingResponse;

import com.smartparking.operation.entity.Booking;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.BookingRepository;

import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepo;
    private final GroupsCustomersRepository groupsCustomersRepository;
    public Page<BookingResponse> getAllBookings(Integer groupId, Integer packageId, Pageable pageable) {
        return bookingRepository.findAllBookingsWithDetails(groupId, packageId, pageable);
    }

    public BookingResponse getBookingDetail(Integer id) {
        throw new UnsupportedOperationException("Chờ ông tạo hàm getDetailById trong Repo nhé!");
    }



    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {

        Booking newBooking = Booking.builder()
                .group(groupsCustomersRepository.findById(request.getGroupId()).orElse(null))
                .packageId(request.getPackageId())
                .build();

        newBooking = bookingRepository.save(newBooking);

        return BookingResponse.builder().id(newBooking.getId()).build();
    }
    @Transactional
    public BookingResponse updateBooking(Integer id, BookingCreateRequest request) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Hợp đồng này!"));

        existing.setGroup(groupsCustomersRepository.findById(request.getGroupId()).orElse(null));
        existing.setPackageId(request.getPackageId());

        bookingRepository.save(existing);
        return BookingResponse.builder().id(existing.getId()).build();
    }

    @Transactional
    public void deleteBooking(Integer id) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Hợp đồng này!"));

        // Hard Delete: Bay màu hoàn toàn theo đúng logic của ông
        bookingRepository.delete(existing);
    }

    public BookingAndDetailResponse getBookingAndDetails(Integer id) {

        BookingResponse bookingInfo = bookingRepository.findBookingDetailById(id)
                .orElseThrow(() -> new BusinessException("Lỗi: Không tìm thấy Hợp đồng (Booking) với ID: " + id));
        List<BookingStatus> targetStatuses = Arrays.asList(
                BookingStatus.ACTIVE,
                BookingStatus.PENDING_ACTIVATION
        );
        List<BookingDetailDto> detailDtos = bookingDetailRepo.findBookingDetailsWithJoinByBookingIdAndStatusIn(id,targetStatuses);

        return BookingAndDetailResponse.builder()
                .bookingInfo(bookingInfo)
                .details(detailDtos)
                .build();
    }

    public List<BookingDetailDto> getBookingDetailsByStatus(Integer bookingId, List<BookingStatus> statuses) {

        // Nếu Frontend không truyền lên, hoặc truyền mảng rỗng -> Lấy tất cả
        if (statuses == null || statuses.isEmpty()) {
            return bookingDetailRepo.findBookingDetailsWithJoinByBookingId(bookingId);
        }

        // Nếu có truyền status cụ thể -> Lọc theo IN
        return bookingDetailRepo.findBookingDetailsWithJoinByBookingIdAndStatusIn(bookingId, statuses);
    }

    public BookingDetailDto getBookingDetailByIdAndGroupId(Integer detailId, Integer groupId) {
        return bookingDetailRepo.findDtoByIdAndGroupId(detailId, groupId)
                .orElseThrow(() -> new BusinessException("Lỗi: Không tìm thấy thông tin xe, hoặc bạn không có quyền truy cập xe này!"));
    }
}
