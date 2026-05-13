package com.smartparking.payment.dto.request.payment;

import lombok.Data;

@Data
public class RenewItemRequest {
    private Integer oldBookingDetailId; // Xe nào?
    private Integer newPackagePriceId;  // Mua gói nào?
}