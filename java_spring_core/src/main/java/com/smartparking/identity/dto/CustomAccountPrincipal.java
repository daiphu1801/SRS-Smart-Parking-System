package com.smartparking.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomAccountPrincipal {
    private Integer accountId;
    private String role;
    private List<Integer> masterGroupIds;
    private List<Integer> memberGroupIds;
    private Integer customerId;
    private Integer employeeId;

}