package com.smartparking.shared.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Zalo ZNS (Zalo Notification Service) integration.
 * TODO: Implement actual ZNS API calls when production keys are available.
 */
@Service
@Slf4j
public class ZaloZNSService {

    public void sendOtp(String phone, String otp) {
        log.info("[ZNS-STUB] Sending OTP {} to phone {}", otp, phone);
        // TODO: integrate with Zalo ZNS REST API
    }

    public void sendSubscriptionConfirmation(String phone, String vehicleNo, String endDate) {
        log.info("[ZNS-STUB] Subscription confirmed: phone={}, plate={}, expires={}",
                phone, vehicleNo, endDate);
    }

    public void sendRenewalReminder(String phone, String vehicleNo, int daysLeft) {
        log.info("[ZNS-STUB] Renewal reminder: phone={}, plate={}, daysLeft={}",
                phone, vehicleNo, daysLeft);
    }
}
