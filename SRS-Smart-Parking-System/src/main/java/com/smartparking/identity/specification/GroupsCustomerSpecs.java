package com.smartparking.identity.specification;

import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsCustomer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class GroupsCustomerSpecs {

    // 1. Lọc gần đúng theo Username (LIKE %...%)
    public static Specification<GroupsCustomer> fetchRelations() {
        return (root, query, cb) -> {
            // MẸO CỰC QUAN TRỌNG: Chỉ fetch khi lấy dữ liệu, KHÔNG fetch khi đếm tổng số bản ghi (cho phân trang)
            // Nếu không có dòng IF này, hàm page.getTotalElements() sẽ báo lỗi nổ tung.
            if (Long.class != query.getResultType()) {
                // Tự động JOIN kéo luôn bảng Profile lên
                root.fetch("groupsProfile", JoinType.LEFT);

                 root.fetch("masterAccount", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }


    public static Specification<GroupsCustomer> hasId(Integer id) {
        return (root, query, cb) -> id != null
                ? cb.equal(root.get("id"), id)
                : cb.conjunction();
    }

    public static Specification<GroupsCustomer> isSynchronize(Boolean isSynchronize) {
        return (root, query, cb) -> isSynchronize != null
                ? cb.equal(root.get("isSynchronize"), isSynchronize)
                : cb.conjunction();
    }

    public static Specification<GroupsCustomer> hasProfileId(Integer profileId) {
        return (root, query, cb) -> profileId != null
                ? cb.equal(root.get("profileId"), profileId)
                : cb.conjunction();
    }

    public static Specification<GroupsCustomer> hasMasterPhone(String masterPhone) {
        return (root, query, cb) -> {
            // Nếu client không truyền tên group -> Bỏ qua bộ lọc này
            if (masterPhone == null || masterPhone.trim().isEmpty()) {
                return cb.conjunction();
            }

            // Thực hiện lệnh JOIN ngầm định (Giống hệt INNER JOIN trong SQL)
            Join<GroupsCustomer, Customer> groupJoin = root.join("masterAccount", JoinType.INNER);

            // Lọc theo cột group_name của bảng groups_customers
            return cb.like(cb.lower(groupJoin.get("userName")), "%" + masterPhone.toLowerCase() + "%");
        };

    }

    public static Specification<GroupsCustomer> hasMasterAccountId(Integer masterAccountId) {
        return (root, query, cb) -> masterAccountId != null
                ? cb.equal(root.get("masterAccountId"), masterAccountId)
                : cb.conjunction();
    }

}