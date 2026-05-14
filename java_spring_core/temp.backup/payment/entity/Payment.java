package com.smartparking.payment.entity;

import com.smartparking.identity.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payer_id", insertable = false, updatable = false)
    private Integer payerId;
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "pay_code", unique = true, length = 50)
    private String payCode;
    @Column(name = "amount",precision = 15, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "gateway")
    private String gateway;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "parking_session_id")
    private Long parkingSessionId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", insertable = false, updatable = false)
    private List<PaymentDetail> details;
}
