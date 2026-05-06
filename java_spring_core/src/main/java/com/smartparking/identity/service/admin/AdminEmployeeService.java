package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.EmployeeCreateRequest;
import com.smartparking.identity.dto.response.EmployeeResponse;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.AccountType;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.EmployeeRepository;
import com.smartparking.identity.specification.EmployeeSpecs;
import com.smartparking.identity.entity.GeneralStatus;
import com.smartparking.shared.dto.PageResponse;
import com.smartparking.shared.integration.SupabaseAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepo;
    private final AccountRepository accountRepo;
    private final SupabaseAuthClient supabaseClient;

    public PageResponse<EmployeeResponse> getEmployees(Pageable pageable, Employee filter) {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecs.hasFullname(filter.getFullName()))
                .and(EmployeeSpecs.hasPhone(filter.getPhone()))
                .and(EmployeeSpecs.hasIsOnline(filter.getIsOnline()));


        Page<Employee> page = employeeRepo.findAll(spec,pageable); // Placeholder

        List<EmployeeResponse> content = page.getContent().stream().map(emp -> EmployeeResponse.builder()
                .id(emp.getId())
                .accountId(emp.getAccountId())
                .fullName(emp.getFullName())
                .isOnline(emp.getIsOnline())
                .createdBy(emp.getCreatedBy())
                .createdAt(emp.getCreatedAt())
                .build()).collect(Collectors.toList());
        return new PageResponse<>(content, page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // 1. Tạo Account dưới DB Local (KHÔNG CẦN GỌI SUPABASE)
        Account pendingAccount = new Account();
        pendingAccount.setUsername(request.getPhone());
        pendingAccount.setAccountType(AccountType.EMPLOYEE);
        pendingAccount.setRoleId(request.getRoleId());

        // TRỌNG TÂM Ở ĐÂY: Để trống Supabase ID vì trên Supabase chưa hề tồn tại người này!
        pendingAccount.setSupabaseId(null);
        pendingAccount.setStatus(GeneralStatus.PENDING); // Trạng thái chờ kích hoạt

        pendingAccount = accountRepo.save(pendingAccount); // Lưu xuống MySQL

        // 2. Tạo Employee dưới DB Local
        Employee newEmp = new Employee();
        newEmp.setPhone(request.getPhone());
        newEmp.setFullName(request.getFullName());
        newEmp.setAccountId(pendingAccount.getId()); // Móc ID vào

        employeeRepo.save(newEmp); // Lưu xuống MySQL
        return EmployeeResponse.builder()
                .id(newEmp.getId())
                .accountId(newEmp.getAccountId())
                .fullName(newEmp.getFullName())
                .phone(request.getPhone())
                .createdBy(newEmp.getCreatedBy())
                .createdAt(newEmp.getCreatedAt())
                .build();
    }
}
