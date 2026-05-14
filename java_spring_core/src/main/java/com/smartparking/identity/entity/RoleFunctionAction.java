package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "role_function_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RoleFunctionAction.RoleFunctionActionId.class)
public class RoleFunctionAction {

    @Id
    @Column(name = "role_id")
    private Integer roleId;

    @Id
    @Column(name = "func_id", insertable = false, updatable = false)
    private Integer funcId;

    @Id
    @Column(name = "action_id", insertable = false, updatable = false)
    private Integer actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

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
