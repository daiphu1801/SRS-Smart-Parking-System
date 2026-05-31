package com.smartparking.shared.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("SEPAY") // 👈 Đây là "chìa khóa" để thằng SystemPaymentService móc ra được
public class SepayPaymentService implements PaymentGatewayService { // 👈 Khai báo kế thừa

    // Giữ nguyên các biến môi trường của sếp
    @Value("${app.sepay.bank-account}")
    private String bankAccount;

    @Value("${app.sepay.bank-name}")
    private String bankName;

    @Value("${app.sepay.account-name}")
    private String accountName;

    // Ghi đè hàm của Interface
    @Override
    public String generateCheckoutUrl(BigDecimal amount, String payCode) {
        String encodedAccountName = "";
        try {
            // Encode tên tài khoản chống lỗi font
            encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            log.error("Lỗi encode account name: {}", e.getMessage());
        }

        // Ép dữ liệu vào Template VietQR chuẩn
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bankName,
                bankAccount,
                amount.longValue(), // Thay biến của interface vào đây
                payCode,            // Nội dung chuyển khoản chính là mã PayCode
                encodedAccountName
        );
    }
}