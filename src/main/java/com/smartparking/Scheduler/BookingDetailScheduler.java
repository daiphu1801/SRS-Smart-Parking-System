import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingDetailScheduler {

    private final BookingDetailRepository bookingDetailRepository;

    /**
     * Chạy vào 00:01 mỗi ngày để cập nhật trạng thái gói cước.
     * Cron expression: "0 1 0 * * *" (Giây - Phút - Giờ - Ngày - Tháng - Thứ)
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void autoUpdateBookingStatuses() {
        LocalDateTime now = LocalDateTime.now();
        log.info("--- BẮT ĐẦU CRONJOB CẬP NHẬT TRẠNG THÁI BOOKING: {} ---", now);

        // JOB 1: KẾT THÚC CÁC GÓI ĐÃ HẾT HẠN
        // Điều kiện: Trạng thái đang ACTIVE và endDate <= thời điểm hiện tại
        List<BookingDetail> expiredDetails = bookingDetailRepository
                .findByStatusAndEndDateBefore(BookingStatus.ACTIVE, now);

        if (!expiredDetails.isEmpty()) {
            expiredDetails.forEach(detail -> detail.setStatus(BookingStatus.COMPLETE));
            bookingDetailRepository.saveAll(expiredDetails);
            log.info(">> Đã kết thúc (COMPLETED) {} gói cước hết hạn.", expiredDetails.size());
        }

        // JOB 2: KÍCH HOẠT CÁC GÓI ĐẾN NGÀY BẮT ĐẦU
        // Điều kiện: Trạng thái đang PENDING_ACTIVATION và startDate <= thời điểm hiện tại
        List<BookingDetail> activationDetails = bookingDetailRepository
                .findByStatusAndStartDateBefore(BookingStatus.PENDING_ACTIVATION, now);

        if (!activationDetails.isEmpty()) {
            activationDetails.forEach(detail -> detail.setStatus(BookingStatus.ACTIVE));
            bookingDetailRepository.saveAll(activationDetails);
            log.info(">> Đã kích hoạt (ACTIVE) {} gói cước mới.", activationDetails.size());
        }

        log.info("--- KẾT THÚC CRONJOB ---");
    }
}