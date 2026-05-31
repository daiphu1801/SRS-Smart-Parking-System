package com.smartparking.shared.service;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    String generateCheckoutUrl(BigDecimal amount, String payCode);
}