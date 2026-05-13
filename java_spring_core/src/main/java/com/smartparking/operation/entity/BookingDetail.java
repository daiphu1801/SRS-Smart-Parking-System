package com.smartparking.operation.entity;

import com.smartparking.identity.entity.Customer;
import com.smartparking.payment.entity.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_details")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "booking_id", insertable = false, updatable = false)
    private Integer bookingId;
    @Column(name = "customer_id", insertable = false, updatable = false)
    private Integer customerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "package_price_id")
    private Integer packagePriceId;
    @Column(name = "vehicle_no", length = 20)
    private String vehicleNo;
    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
