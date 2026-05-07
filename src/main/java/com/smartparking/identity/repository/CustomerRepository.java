package com.smartparking.identity.repository;

import com.smartparking.identity.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartparking.identity.entity.Customer;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByAccountId(Integer accountId);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByFullName(String username);

    @Query("SELECT c.groupId FROM Customer c WHERE c.accountId = :accountId AND c.groupId IS NOT NULL")
    List<Integer> findMemberGroupIdsByAccountId(@Param("accountId") Integer accountId);
}
