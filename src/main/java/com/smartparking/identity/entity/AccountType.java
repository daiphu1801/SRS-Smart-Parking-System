package com.smartparking.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


public enum AccountType {
    CUSTOMER,EMPLOYEE;

    public static AccountType fromString(String status) {
        for (AccountType s : AccountType.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }

        return EMPLOYEE;
    }
}
