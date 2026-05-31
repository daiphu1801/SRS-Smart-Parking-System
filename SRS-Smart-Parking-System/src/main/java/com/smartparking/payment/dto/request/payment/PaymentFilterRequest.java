package com.smartparking.payment.dto.request.payment;

import com.smartparking.payment.entity.PaymentMethod;
import com.smartparking.payment.entity.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentFilterRequest {
    // Thông tin người thanh toán
    private Integer customerId; // Tương ứng payer_id
    private String customerPhone;

    // Thông tin giao dịch
    private String payCode;
    private Status payStatus;
    private String gateway;
    private PaymentMethod method;
    private Long parkingSessionId;
    // Khoảng số tiền
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Khoảng thời gian tạo
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;

    // Khoảng thời gian cập nhật (rất cần thiết khi tra cứu lúc nào thanh toán thành công)
    private LocalDateTime updatedAtFrom;
    private LocalDateTime updatedAtTo;
}