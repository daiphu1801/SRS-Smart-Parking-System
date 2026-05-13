package com.smartparking.payment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CheckoutRequest {

    @NotEmpty(message = "Danh sách thanh toán không được để trống")
    private List<Integer> bookingDetailIds;
}