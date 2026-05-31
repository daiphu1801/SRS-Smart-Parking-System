package com.smartparking.identity.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.smartparking.identity.entity.GeneralStatus;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Account extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "supabase_id", length = 50)
    private UUID supabaseId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "address")
    private String address;


    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GeneralStatus status = GeneralStatus.ACTIVE;

    @NotAudited
    @Column(name = "last_login")
    private LocalDateTime lastLogin;



    public String getRoleName() {
        return accountType.name();
    }

    public static Account createCustomer(String phone) {
        return Account.builder()
                .username(phone)
                .accountType(AccountType.CUSTOMER)
                .status(GeneralStatus.ACTIVE)
                .build();
    }
}
