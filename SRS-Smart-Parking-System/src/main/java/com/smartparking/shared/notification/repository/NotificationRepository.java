package com.smartparking.shared.notification.repository;

import com.smartparking.shared.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findTop20ByAccountIdOrderByCreatedAtDesc(Integer accountId);
}