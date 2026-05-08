package com.smartparking.shared.controller;

import com.smartparking.shared.entity.Notification;
import com.smartparking.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestAttribute("accountId") Integer accountId) {
        return ResponseEntity.ok(notificationService.getMyNotifications(accountId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(@RequestAttribute("accountId") Integer accountId) {
        notificationService.markAllAsRead(accountId);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }
}
