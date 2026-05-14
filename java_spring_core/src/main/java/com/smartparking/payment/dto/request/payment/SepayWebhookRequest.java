package com.smartparking.payment.dto.request.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SepayWebhookRequest {
    private String gateway;         // Tên ngân hàng (VD: MBBank)
    private String transactionDate; // Thời gian GD
    private String transactionId;
    private String accountNumber;   // Số TK nhận
    private String content;         // NỘI DỤNG CHUYỂN KHOẢN (Chứa payCode)
    private BigDecimal transferAmount; // SỐ TIỀN KHÁCH CHUYỂN
    private String referenceCode;   // Mã tham chiếu ngân hàng
}