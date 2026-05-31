package com.smartparking.shared.notification.service;

import com.smartparking.shared.notification.entity.Notification;
import com.smartparking.shared.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // CÁCH 1: GỬI ĐÍCH DANH CHO 1 ACCOUNT
    public void sendDirectNotification(Integer targetAccountId, String title, String content) {
        Notification notif = Notification.builder()
                .accountId(targetAccountId)
                .title(title)
                .content(content)
                .type("DIRECT_MSG")
                .build();

        notificationRepository.save(notif);
        log.info("[🔔 NOTIFICATION] Đã lưu thông báo đích danh cho Account ID: {}", targetAccountId);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendBroadcastByPermission(String requiredPermission, String title, String content) {
        Notification notif = Notification.builder()
                .requiredPermission(requiredPermission) // Ví dụ: "COMPLAINT_RECEIVE"
                .title(title)
                .content(content)
                .type("BROADCAST_ALERT")
                .build();

        notificationRepository.save(notif);
        log.info("[📢 NOTIFICATION] Đã phát thanh thông báo cho những người có quyền: {}", requiredPermission);
    }

    public List<Notification> getMyNotifications(Integer accountId) {
        log.info("Lấy danh sách thông báo cho Account ID: {}", accountId);
        return notificationRepository.findTop20ByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional
    public void saveAll(List<Notification> notifications) {
        if (notifications != null && !notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
            log.info("[🔔 NOTIFICATION] Đã lưu thành công lô {} thông báo.", notifications.size());
        }
    }
}