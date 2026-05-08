package com.smartparking.shared.repository;

import com.smartparking.shared.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccountIdOrderByCreatedAtDesc(Integer accountId);
    List<Notification> findByAccountIdAndIsReadFalse(Integer accountId);
}
