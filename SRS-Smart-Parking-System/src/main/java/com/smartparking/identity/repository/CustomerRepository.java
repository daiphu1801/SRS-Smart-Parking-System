package com.smartparking.identity.repository;

import com.smartparking.identity.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartparking.identity.entity.Customer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByAccountId(Integer accountId);

    @EntityGraph(attributePaths = {"groupsCustomer"})
    Page<Customer> findAll(Specification<Customer> spec, Pageable pageable);

    @Query("SELECT c.groupId FROM Customer c WHERE c.accountId = :accountId AND c.groupId IS NOT NULL")
    List<Integer> findMemberGroupIdsByAccountId(@Param("accountId") Integer accountId);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.groupsCustomer WHERE c.id = :id")
    Optional<Customer> findCustomerWithGroupById(@Param("id") Integer id);

    List<Customer> findByAccountIdIn(Set<Integer> accountIds);
}
