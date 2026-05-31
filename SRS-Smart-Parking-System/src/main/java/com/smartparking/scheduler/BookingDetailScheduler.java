package com.smartparking.scheduler;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.operation.repository.BookingDetailRepository;
import com.smartparking.shared.notification.entity.Notification;
import com.smartparking.shared.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/jobs")
@Slf4j
public class BookingDetailScheduler {

    private final BookingDetailRepository bookingDetailRepository;
    private final NotificationService notificationService;

    @PostMapping("/update-booking-status")
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

    @PostMapping("/send-expired-booking-notification")
    public void autoNotifyExpiringBookings() {
        log.info("--- BẮT ĐẦU CRONJOB GỬI THÔNG BÁO NHẮC GIA HẠN ---");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateEnd = LocalDate.now().plusDays(3).atTime(23, 59, 59);

        List<BookingDetail> expiringDetails = bookingDetailRepository.findExpiringBookings(
                BookingStatus.ACTIVE,
                now,
                targetDateEnd
        );

        if (expiringDetails.isEmpty()) {
            log.info(">> Không có gói cước nào sắp hết hạn trong 3 ngày tới.");
            return;
        }

        List<Notification> notificationsToSave = new ArrayList<>();

        for (BookingDetail detail : expiringDetails) {
            Integer accountId = detail.getCustomer().getAccountId();
            String vehicleNo = detail.getVehicleNo();

            Notification notif = Notification.builder()
                    .accountId(accountId)
                    .requiredPermission("BOOKING_READ")
                    .title("Sắp hết hạn gói cước đỗ xe")
                    .content("Gói cước đỗ xe cho biển số " + vehicleNo + " sẽ hết hạn sau 3 ngày nữa. Vui lòng gia hạn để không bị gián đoạn dịch vụ!")
                    .type("BROADCAST_ALERT")
                    .build();
            notificationsToSave.add(notif);
        }

        notificationService.saveAll(notificationsToSave);

        log.info(">> Đã gửi thành công {} thông báo nhắc gia hạn.", notificationsToSave.size());
        log.info("--- KẾT THÚC CRONJOB GỬI THÔNG BÁO ---");
    }
}