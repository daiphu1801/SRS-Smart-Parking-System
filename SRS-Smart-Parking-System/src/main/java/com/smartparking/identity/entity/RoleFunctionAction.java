package com.smartparking.identity.entity;

import com.smartparking.shared.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "role_function_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RoleFunctionAction.RoleFunctionActionId.class)
@Audited
public class RoleFunctionAction extends BaseAuditEntity {

    @Id
    @Column(name = "role_id")
    private Integer roleId;

    @Id
    @Column(name = "func_id", insertable = false, updatable = false)
    private Integer funcId;

    @Id
    @Column(name = "action_id", insertable = false, updatable = false)
    private Integer actionId;


    @Audited(targetAuditMode = NOT_AUDITED)
    @MapsId("actionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Action action;

    @Audited(targetAuditMode = NOT_AUDITED)
    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @MapsId("funcId")
    @Audited(targetAuditMode = NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "func_id")
    private Function function;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleFunctionActionId implements Serializable {
        private Integer roleId;
        private Integer funcId;
        private Integer actionId;
    }
}
