package com.smartparking.scheduler;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    public void autoUpdateBookingStatuses() {

        // Ép về đúng 00:00:00 của ngày hôm nay để so sánh
        LocalDateTime todayMidnight = LocalDate.now().atStartOfDay();
        log.info("--- BẮT ĐẦU CRONJOB CẬP NHẬT TRẠNG THÁI BOOKING TẠI MỐC: {} ---", todayMidnight);
        int expiredCount = bookingDetailRepository.updateExpiredBookings(
                BookingStatus.ACTIVE,
                BookingStatus.COMPLETE,
                todayMidnight
        );
        if (expiredCount > 0) {
            log.info(">> Đã tự động kết thúc (COMPLETE) {} gói cước hết hạn.", expiredCount);
        }

        int activatedCount = bookingDetailRepository.updateActiveBookings(
                BookingStatus.PENDING_ACTIVATION,
                BookingStatus.ACTIVE,
                todayMidnight
        );
        if (activatedCount > 0) {
            log.info(">> Đã tự động kích hoạt (ACTIVE) {} gói cước mới.", activatedCount);
        }

        log.info("--- KẾT THÚC CRONJOB ---");
    }
}