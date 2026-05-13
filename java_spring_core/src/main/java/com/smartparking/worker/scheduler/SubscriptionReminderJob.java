package com.smartparking.worker.scheduler;

import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.shared.integration.ZaloZNSService;
import com.smartparking.operation.repository.BookingDetailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderJob {

    private final BookingDetailRepository bookingDetailRepo;
    private final CustomerRepository customerRepo;
    private final ZaloZNSService znsService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendRenewalReminders() {
        log.info("Running SubscriptionReminderJob...");
        var expiring = bookingDetailRepo.findExpiringBetween(
                LocalDateTime.now(), LocalDateTime.now().plusDays(5));
        log.info("Found {} subscriptions expiring in 5 days", expiring.size());

        for (var detail : expiring) {
            try {
                customerRepo.findById(detail.getCustomerId()).ifPresent(customer -> {
                    int daysLeft = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), detail.getEndDate());
                    znsService.sendRenewalReminder(customer.getPhone(), detail.getVehicleNo(), daysLeft);
                    log.info("Sent reminder to {} for plate {}", customer.getPhone(), detail.getVehicleNo());
                });
            } catch (Exception e) {
                log.error("Failed to send reminder for booking {}: {}", detail.getId(), e.getMessage());
            }
        }
    }
}
