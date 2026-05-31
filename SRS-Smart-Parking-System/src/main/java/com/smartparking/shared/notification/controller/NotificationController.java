package com.smartparking.shared.notification.controller;


import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.CustomAccountPrincipal;
import com.smartparking.shared.notification.entity.Notification;
import com.smartparking.shared.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @AuthenticationPrincipal CustomAccountPrincipal principal) {
        Integer accountId = principal.getAccountId();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách thông báo thành công",
                notificationService.getMyNotifications(accountId)
        ));
    }

}
