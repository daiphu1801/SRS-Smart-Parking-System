package com.smartparking.payment.dto.request.payment;

import com.smartparking.payment.entity.PaymentMethod;
import lombok.Data;

@Data
public class PaymentSessionRequest {
    // Chỉ cần đúng cái ID lượt đỗ xe của khách vãng lai
    private Long parkingSessionId;

    private String gateway;
    private String returnUrl;

    private PaymentMethod method;
}