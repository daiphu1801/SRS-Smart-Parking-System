package com.smartparking.identity.repository;

import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByPhone(String phone);
    Optional<Employee> findByAccountId(Integer accountId);
}
