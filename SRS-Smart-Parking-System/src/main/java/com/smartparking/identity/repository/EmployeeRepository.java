package com.smartparking.identity.repository;

import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByPhone(String phone);
    Optional<Employee> findByAccountId(Integer accountId);
    List<Employee> findByAccountIdIn(Set<Integer> accountIds);

}
