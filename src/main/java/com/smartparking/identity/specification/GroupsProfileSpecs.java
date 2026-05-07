package com.smartparking.identity.specification;

import com.smartparking.identity.entity.GroupsProfile;
import com.smartparking.identity.entity.GroupsCustomer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class GroupsProfileSpecs {

    // 1. Lọc gần đúng theo Username (LIKE %...%)
    public static Specification<GroupsProfile> hasProfileName(String profileName) {
        return (root, query, cb) -> StringUtils.hasText(profileName)
                ? cb.like(root.get("profileName"), "%" + profileName + "%")
                : cb.conjunction();
    }

    // 2. Lọc chính xác theo Role ID
    public static Specification<GroupsProfile> hasProfileCode(String profileCode) {
        return (root, query, cb) -> profileCode != null
                ? cb.like(root.get("profileCode"), "%" + profileCode + "%")
                : cb.conjunction();
    }

    public static Specification<GroupsProfile> hasId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("accountId"), id)
                : cb.conjunction();
    }


}