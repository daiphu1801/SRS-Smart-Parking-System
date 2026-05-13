package com.smartparking.payment.service;

import java.math.BigDecimal;
import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class VietQRService {

    @Value("${app.sepay.bank-account}")
    private String bankAccount;

    @Value("${app.sepay.bank-name}")
    private String bankName;

    @Value("${app.sepay.account-name}")
    private String accountName;

    public String generateQrUrl(BigDecimal totalAmount, String transferContent) {
        String encodedAccountName = "";
        try {
            // Sử dụng đúng logic Encode ông đưa ra
            encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            log.error("Lỗi encode account name: {}", e.getMessage());
        }

        // Trả về đúng template ảnh VietQR chuẩn
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bankName,
                bankAccount,
                totalAmount.longValue(),
                transferContent,
                encodedAccountName
        );
    }
}
