package com.smartparking.operation.service.admin;

import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.dto.request.BookingCreateRequest;
import com.smartparking.operation.dto.response.BookingAndDetailResponse;
import com.smartparking.operation.dto.response.BookingResponse;

import com.smartparking.operation.entity.Booking;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.operation.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepo;
    private final GroupsCustomersRepository groupsCustomersRepository;
    // Thêm các repo của Group, Package để validate nếu cần

    public Page<BookingResponse> getAllBookings(Integer groupId, Integer packageId, Pageable pageable) {
        // Trả thẳng DTO từ câu Query JOIN
        return bookingRepository.findAllBookingsWithDetails(groupId, packageId, pageable);
    }

    public BookingResponse getBookingDetail(Integer id) {
        // Thực tế nếu Admin ấn vào xem chi tiết, ông có thể viết 1 câu Query JOIN y hệt trên
        // nhưng trả về 1 Object thay vì Page. Hoặc tái sử dụng hàm trên với PageRequest.of(0, 1)
        throw new UnsupportedOperationException("Chờ ông tạo hàm getDetailById trong Repo nhé!");
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        // TODO: Móc Account ID từ SecurityContextHolder (người đang đăng nhập)
        Integer adminId = 1; // Mock tạm

        Booking newBooking = Booking.builder()
                .group(groupsCustomersRepository.findById(request.getGroupId()).orElse(null))
                .packageId(request.getPackageId())
                .createdBy(adminId)
                .build();

        newBooking = bookingRepository.save(newBooking);

        // Trả về response (có thể query lại để lấy Full tên, hoặc tự build chay)
        return BookingResponse.builder().id(newBooking.getId()).build();
    }

    @Transactional
    public BookingResponse updateBooking(Integer id, BookingCreateRequest request) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng này!"));

        existing.setGroup(groupsCustomersRepository.findById(request.getGroupId()).orElse(null));
        existing.setPackageId(request.getPackageId());

        bookingRepository.save(existing);
        return BookingResponse.builder().id(existing.getId()).build();
    }

    @Transactional
    public void deleteBooking(Integer id) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hợp đồng này!"));

        // Hard Delete: Bay màu hoàn toàn theo đúng logic của ông
        bookingRepository.delete(existing);
    }

    public BookingAndDetailResponse getBookingAndDetails(Integer id) {

        BookingResponse bookingInfo = bookingRepository.findBookingDetailById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Hợp đồng (Booking) với ID: " + id));

        List<BookingDetailDto> detailDtos = bookingDetailRepo.findBookingDetailsWithJoinByBookingId(id);

        return BookingAndDetailResponse.builder()
                .bookingInfo(bookingInfo)
                .details(detailDtos)
                .build();
    }
}
