package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.EmployeeCreateRequest;
import com.smartparking.identity.dto.response.EmployeeResponse;
import com.smartparking.identity.entity.*;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.EmployeeRepository;
import com.smartparking.identity.specification.EmployeeSpecs;
import com.smartparking.shared.dto.PageResponse;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.integration.SupabaseAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepo;
    private final AccountRepository accountRepo;

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
                .phone(emp.getPhone())
                .createdBy(emp.getCreatedBy())
                .createdAt(emp.getCreatedAt())
                .deleted(emp.getDeleted())
                .deletedAt(emp.getDeletedAt())
                .build()).collect(Collectors.toList());
        return new PageResponse<>(content, page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // 1. Tạo Account dưới DB Local (KHÔNG CẦN GỌI SUPABASE)
        if (request.getPhone().startsWith("0")) {
            request.setPhone("84" + request.getPhone().substring(1));
        };
        Account pendingAccount = new Account();
        pendingAccount.setUsername(request.getPhone());
        pendingAccount.setAccountType(AccountType.EMPLOYEE);
        pendingAccount.setRoleId(request.getRoleId());


        // TRỌNG TÂM Ở ĐÂY: Để trống Supabase ID vì trên Supabase chưa hề tồn tại người này!
        pendingAccount.setSupabaseId(null);
        pendingAccount.setStatus(GeneralStatus.ACTIVE); // Trạng thái chờ kích hoạt

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

    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên với ID: " + id));

        // Ở đây ông dùng Mapper (như MapStruct) hoặc tự build tay từ Entity sang Response
        return EmployeeResponse.builder()
                .id(employee.getId())
                .accountId(employee.getAccountId())
                .fullName(employee.getFullName())
                .phone(employee.getPhone())
                .createdBy(employee.getCreatedBy())
                .createdAt(employee.getCreatedAt())
                .build();
    }

    // HÀM XÓA MỀM
    @Transactional
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        employee.setDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepo.save(employee);

        Integer accountId = employee.getAccountId();

        if (accountId != null) {
            accountRepo.findById(accountId).ifPresent(account -> {
                account.setStatus(GeneralStatus.LOCKED);
                accountRepo.save(account);
            });
        }
    }
@Transactional
    public Employee updateEmployee(Integer id, Employee employeeUpdate) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Employee not found"));
        employee.setFullName(employeeUpdate.getFullName());
        return employeeRepo.save(employee);
    }
}
