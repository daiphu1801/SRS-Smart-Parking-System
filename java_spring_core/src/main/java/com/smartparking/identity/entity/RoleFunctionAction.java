package com.smartparking.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "role_function_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RoleFunctionAction.RoleFunctionActionId.class)
public class RoleFunctionAction {

    @Id
    @Column(name = "role_id")
    private Integer roleId;

    @Id
    @Column(name = "func_id")
    private Integer funcId;

    @Id
    @Column(name = "action_id")
    private Integer actionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleFunctionActionId implements Serializable {
        private Integer roleId;
        private Integer funcId;
        private Integer actionId;
    }
}
