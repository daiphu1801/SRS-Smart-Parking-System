package com.smartparking.identity.specification;

import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsCustomer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CustomerSpecs {

    // 1. Lọc gần đúng theo Username (LIKE %...%)
    public static Specification<Customer> hasFullname(String fullName) {
        return (root, query, cb) -> StringUtils.hasText(fullName)
                ? cb.like(root.get("fullName"), "%" + fullName + "%")
                : cb.conjunction();
    }

    // 2. Lọc chính xác theo Role ID
    public static Specification<Customer> hasPhone(String phone) {
        return (root, query, cb) -> phone != null
                ? cb.like(root.get("phone"), "%" + phone + "%")
                : cb.conjunction();
    }

    public static Specification<Customer> hasAccountId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("accountId"), id)
                : cb.conjunction();
    }
    public static Specification<Customer> hasGroupId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("groupId"), id)
                : cb.conjunction();
    }
    
    public static Specification<Customer> hasDeleted(Boolean deleted) {
        return (root, query, cb) -> deleted != null
                ? cb.equal(root.get("deleted"), deleted)
                : cb.conjunction();
    }


    public static Specification<Customer> hasAddress(String address) {
        return (root, query, cb) -> address != null
                ? cb.like(root.get("address"), "%" + address + "%")
                : null;
    }

    public static Specification<Customer> hasGroupName(String groupName) {
        return (root, query, cb) -> {
            // Nếu client không truyền tên group -> Bỏ qua bộ lọc này
            if (groupName == null || groupName.trim().isEmpty()) {
                return cb.conjunction();
            }

            // Thực hiện lệnh JOIN ngầm định (Giống hệt INNER JOIN trong SQL)
            Join<Customer, GroupsCustomer> groupJoin = root.join("groupsCustomer", JoinType.INNER);

            // Lọc theo cột group_name của bảng groups_customers
            return cb.like(cb.lower(groupJoin.get("groupName")), "%" + groupName.toLowerCase() + "%");
        };
    }

}