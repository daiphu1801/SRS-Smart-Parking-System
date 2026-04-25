package com.smartparking.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * SePay VietQR Integration
 *
 * SePay cho phép tạo QR code dạng deeplink với số tài khoản + nội dung chuyển khoản.
 * Khi có giao dịch match vào tài khoản, SePay gửi Webhook về endpoint của backend.
 *
 * Tài liệu: https://docs.sepay.vn/
 */
@Service
@Slf4j
public class SePayService {

    @Value("${app.sepay.bank-account}")
    private String bankAccount;

    @Value("${app.sepay.bank-name}")
    private String bankName;

    @Value("${app.sepay.account-name}")
    private String accountName;

    @Value("${app.sepay.webhook-secret}")
    private String webhookSecret;

    /**
     * Tạo VietQR deeplink.
     * SePay sử dụng VietQR standard — không cần gọi API tạo QR,
     * chỉ cần build đúng URL format.
     *
     * Format: https://qr.sepay.vn/img?bank=BANK&acc=ACCOUNT&template=compact&amount=AMOUNT&des=CONTENT
     */
    public String createQrImageUrl(long amount, String transactionContent) {
        String encoded = transactionContent.replace(" ", "%20");
        return String.format(
            "https://qr.sepay.vn/img?bank=%s&acc=%s&template=compact&amount=%d&des=%s",
            bankName, bankAccount, amount, encoded
        );
    }

    /**
     * Xác thực webhook từ SePay.
     * SePay gửi header "Authorization: Apikey {YOUR_API_KEY}"
     */
    public boolean isValidWebhook(String apiKeyHeader) {
        if (apiKeyHeader == null) return false;
        // SePay dùng: Authorization: Apikey <token>
        String token = apiKeyHeader.replace("Apikey ", "").trim();
        return webhookSecret.equals(token);
    }

    /**
     * Parse transaction content từ webhook payload để match với payment code.
     * SePay webhook payload có field "content" chứa nội dung chuyển khoản.
     */
    public String extractPaymentCode(Map<String, Object> payload) {
        Object content = payload.get("content");
        if (content == null) return null;
        // Nội dung dạng "SMARTPARK {payCode} ..." — extract payCode
        String[] parts = content.toString().split("\\s+");
        return parts.length > 1 ? parts[1] : null;
    }
}
