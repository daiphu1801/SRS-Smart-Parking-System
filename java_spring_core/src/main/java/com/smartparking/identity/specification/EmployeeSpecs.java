package com.smartparking.identity.specification;

import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.AccountType;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.entity.GeneralStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class EmployeeSpecs {

    // 1. Lọc gần đúng theo Username (LIKE %...%)
    public static Specification<Employee> hasFullname(String fullName) {
        return (root, query, cb) -> StringUtils.hasText(fullName)
                ? cb.like(root.get("fullName"), "%" + fullName + "%")
                : null;
    }

    // 2. Lọc chính xác theo Role ID
    public static Specification<Employee> hasPhone(String phone) {
        return (root, query, cb) -> phone != null
                ? cb.like(root.get("phone"), "%" + phone + "%")
                : null;
    }

    // 3. Lọc theo trạng thái Online (Boolean)
    public static Specification<Employee> hasIsOnline(Boolean isOnline) {
        return (root, query, cb) -> isOnline != null
                ? cb.equal(root.get("isOnline"), isOnline)
                : null;
    }

}