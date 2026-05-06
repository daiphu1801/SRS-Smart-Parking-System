package com.smartparking.identity.specification;

import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.AccountType;
import com.smartparking.identity.entity.GeneralStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class AccountSpecs {

    // 1. Lọc gần đúng theo Username (LIKE %...%)
    public static Specification<Account> hasUsername(String username) {
        return (root, query, cb) -> StringUtils.hasText(username)
                ? cb.like(root.get("username"), "%" + username + "%")
                : null;
    }

    // 2. Lọc chính xác theo Role ID
    public static Specification<Account> hasRole(Integer roleId) {
        return (root, query, cb) -> roleId != null
                ? cb.equal(root.get("roleId"), roleId)
                : null;
    }

    // 3. Lọc chính xác theo Account Type (Enum)
    public static Specification<Account> hasAccountType(String accountType) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(accountType)) return null;
            try {
                return cb.equal(root.get("accountType"), AccountType.valueOf(accountType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    // 4. Lọc chính xác theo Status (Enum)
    public static Specification<Account> hasStatus(String status) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(status)) return null;
            try {
                return cb.equal(root.get("status"), GeneralStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }
}