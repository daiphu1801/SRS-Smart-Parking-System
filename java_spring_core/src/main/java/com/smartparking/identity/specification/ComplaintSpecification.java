package com.smartparking.identity.specification;

import com.smartparking.identity.dto.request.ComplaintFilterRequest;
import com.smartparking.identity.entity.Complaint;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ComplaintSpecification {

    public static Specification<Complaint> buildFilterSpec(ComplaintFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }
            if (filter.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }
            if (filter.getCreatedBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.getCreatedBy()));
            }
            if (filter.getSolvedBy() != null) {
                predicates.add(cb.equal(root.get("solvedBy"), filter.getSolvedBy()));
            }
            if (filter.getIsSolved() != null) {
                predicates.add(cb.equal(root.get("isSolved"), filter.getIsSolved()));
            }

            // Sắp xếp mặc định: Mới nhất lên đầu
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}