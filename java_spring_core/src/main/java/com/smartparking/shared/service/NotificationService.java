package com.smartparking.shared.service;

import com.smartparking.shared.entity.Notification;
import com.smartparking.shared.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public List<Notification> getMyNotifications(Integer accountId) {
        return notificationRepo.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public void markAsRead(Long id) {
        notificationRepo.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepo.save(n);
        });
    }

    public void markAllAsRead(Integer accountId) {
        notificationRepo.findByAccountIdAndIsReadFalse(accountId).forEach(n -> {
            n.setIsRead(true);
            notificationRepo.save(n);
        });
    }
}
