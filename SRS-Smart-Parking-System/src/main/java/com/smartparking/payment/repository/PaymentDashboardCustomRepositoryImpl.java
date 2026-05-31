package com.smartparking.payment.repository;

import com.smartparking.payment.dto.response.dashboard.PaymentKpiResponse;
import com.smartparking.payment.dto.response.dashboard.PaymentMethodShareResponse;
import com.smartparking.payment.dto.response.dashboard.RevenueTimeSeriesResponse;
import com.smartparking.payment.dto.response.dashboard.TopCustomerRevenueResponse;
import com.smartparking.payment.entity.Payment;
import com.smartparking.payment.entity.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentDashboardCustomRepositoryImpl implements PaymentDashboardCustomRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public PaymentKpiResponse getPaymentKpis(Specification<Payment> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PaymentKpiResponse> query = cb.createQuery(PaymentKpiResponse.class);
        Root<Payment> root = query.from(Payment.class);

        // 1. TÁI SỬ DỤNG BỘ LỌC CỦA SẾP
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) query.where(predicate);
        }

        // 2. ÉP KIỂU VỀ DTO (SUM, COUNT)
        Expression<BigDecimal> sumRevenue = cb.coalesce(cb.sum(root.get("amount")), BigDecimal.ZERO);
        Expression<Long> totalTx = cb.count(root.get("id"));

        // Đếm giao dịch thành công
        Expression<Long> successTx = cb.count(
                cb.selectCase().when(cb.equal(root.get("status"), Status.SUCCESS), 1).otherwise((Integer) null)
        );
        // Đếm giao dịch thất bại
        Expression<Long> failedTx = cb.count(
                cb.selectCase().when(root.get("status").in(Status.FAILED, Status.CANCELED), 1).otherwise((Integer) null)
        );

        query.multiselect(sumRevenue, totalTx, successTx, failedTx);

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public List<PaymentMethodShareResponse> getMethodShares(Specification<Payment> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PaymentMethodShareResponse> query = cb.createQuery(PaymentMethodShareResponse.class);
        Root<Payment> root = query.from(Payment.class);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) query.where(predicate);
        }

        // Dùng GROUP BY phương thức thanh toán
        Expression<String> methodExpr = cb.coalesce(root.get("method").as(String.class), "UNKNOWN");
        Expression<BigDecimal> sumAmount = cb.coalesce(cb.sum(root.get("amount")), BigDecimal.ZERO);

        query.multiselect(methodExpr, sumAmount, cb.literal(0.0)) // % sẽ tính trên Service cho dễ
                .groupBy(methodExpr)
                .orderBy(cb.desc(sumAmount));

        return em.createQuery(query).getResultList();
    }

    @Override
    public List<TopCustomerRevenueResponse> getTopCustomers(Specification<Payment> spec, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TopCustomerRevenueResponse> query = cb.createQuery(TopCustomerRevenueResponse.class);
        Root<Payment> root = query.from(Payment.class);

        // Bắt buộc Join Customer để lấy thông tin
        Join<Object, Object> customerJoin = root.join("customer", JoinType.INNER);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) query.where(predicate);
        }

        Expression<BigDecimal> sumAmount = cb.coalesce(cb.sum(root.get("amount")), BigDecimal.ZERO);
        Expression<Long> txCount = cb.count(root.get("id"));

        query.multiselect(
                        customerJoin.get("id"),
                        customerJoin.get("phone"),
                        sumAmount,
                        txCount
                )
                .groupBy(customerJoin.get("id"), customerJoin.get("phone"))
                .orderBy(cb.desc(sumAmount));

        return em.createQuery(query).setMaxResults(limit).getResultList();
    }

    @Override
    public List<RevenueTimeSeriesResponse> getRevenueTimeSeries(Specification<Payment> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<RevenueTimeSeriesResponse> query = cb.createQuery(RevenueTimeSeriesResponse.class);
        Root<Payment> root = query.from(Payment.class);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) query.where(predicate);
        }

        Expression<String> dateExpr = cb.function(
                "to_char",
                String.class,
                root.get("createdAt"),
                cb.literal("YYYY-MM-DD")
        );

        Expression<BigDecimal> sumAmount = cb.coalesce(cb.sum(root.get("amount")), BigDecimal.ZERO);
        Expression<Long> txCount = cb.count(root.get("id"));

        query.multiselect(dateExpr, sumAmount, txCount)
                .groupBy(dateExpr)
                .orderBy(cb.asc(dateExpr));

        return em.createQuery(query).getResultList();
    }
}