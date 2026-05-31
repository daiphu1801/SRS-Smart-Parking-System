package com.smartparking.identity.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Customer extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "account_id", unique = true)
    private Integer accountId;
    @Column(name = "group_id", insertable = false, updatable = false)
    private Integer groupId;
    @Column(name = "full_name", length = 100)
    private String fullName;
    @Column(length = 15, unique = true)
    private String phone;
    @Column(length = 255)
    private String address;

    @Column(name = "deleted")
    private Boolean deleted;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private GroupsCustomer groupsCustomer;
}
