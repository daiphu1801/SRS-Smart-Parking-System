package com.smartparking.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_details")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "payment_id") private Long paymentId;
    @Column(name = "booking_detail_id") private Integer bookingDetailId;
    @Column(name = "parking_session_id") private Long parkingSessionId;
    @Column(name = "item_amount", precision = 15, scale = 2) private BigDecimal itemAmount;
    @Column(name = "applied_start_date") private LocalDateTime appliedStartDate;
    @Column(name = "applied_end_date") private LocalDateTime appliedEndDate;
}
