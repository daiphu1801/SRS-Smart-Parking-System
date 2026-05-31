package com.smartparking.payment.dto.response.payment;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaymentTreeResponse {
    // Thông tin của Node Cha (Payment)
    private PaymentResponse paymentInfo;

    // Danh sách Node Con (Payment Details)
    private List<PaymentDetailResponse> details;
}