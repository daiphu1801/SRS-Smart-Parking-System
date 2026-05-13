package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.CustomerCreateRequest;
import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.dto.response.EmployeeResponse;
import com.smartparking.identity.entity.*;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.identity.specification.CustomerSpecs;
import org.springframework.data.jpa.domain.Specification;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final GroupsCustomersRepository groupRepository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getCustomers(Pageable pageable, Customer filter, String groupName) {
        Specification<Customer> spec = Specification
                .where(CustomerSpecs.hasFullname(filter.getFullName()))
                .and(CustomerSpecs.hasPhone(filter.getPhone()))
                .and(CustomerSpecs.hasAddress(filter.getAddress()))
                .and(CustomerSpecs.hasGroupId(filter.getGroupId()))
                .and(CustomerSpecs.hasGroupName(groupName))
                .and(CustomerSpecs.hasAccountId(filter.getAccountId()));


        Page<Customer> page = customerRepo.findAll(spec, pageable);

        List<CustomerResponse> content = page.getContent().stream().map(customer -> CustomerResponse.builder()
                .id(customer.getId())
                .accountId(customer.getAccountId())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .groupName(customer.getGroupsCustomer() != null ? customer.getGroupsCustomer().getGroupName() : null)
                .groupId(customer.getGroupId())
                .createdAt(customer.getCreatedAt())
                .deleted(customer.getDeleted())
                .deletedAt(customer.getDeletedAt())
                .build()).collect(Collectors.toList());
        return new PageResponse<>(content, page.getTotalElements(), page.getTotalPages());
    }

    public CustomerResponse getCustomersById(Integer id) {
        Customer customer = customerRepo.findCustomerWithGroupById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        // Ở đây ông dùng Mapper (như MapStruct) hoặc tự build tay từ Entity sang Response
        return CustomerResponse.builder()
                .id(customer.getId())
                .accountId(customer.getAccountId())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .groupName(customer.getGroupsCustomer() != null ? customer.getGroupsCustomer().getGroupName() : null)
                .groupId(customer.getGroupId())
                .createdAt(customer.getCreatedAt())
                .deleted(customer.getDeleted())
                .deletedAt(customer.getDeletedAt())
                .build();
    }


    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {

        Account pendingAccount = new Account();
        if (request.getPhone().startsWith("0")) {
            request.setPhone("84" + request.getPhone().substring(1));
        };
        pendingAccount.setUsername(request.getPhone());
        pendingAccount.setAccountType(AccountType.CUSTOMER);
        pendingAccount.setRoleId(request.getRoleId());
        pendingAccount.setSupabaseId(null);
        pendingAccount.setStatus(GeneralStatus.ACTIVE); // Trạng thái chờ kích hoạt

        pendingAccount = accountRepo.save(pendingAccount);

        Customer customer = new Customer();
        customer.setPhone(request.getPhone());
        customer.setFullName(request.getFullName());
        customer.setAccountId(pendingAccount.getId());
        customer.setGroupId(request.getGroupId());
        customer.setAddress(request.getAddress());
        customer.setGroupsCustomer(groupRepository.getReferenceById(request.getGroupId()));

        customerRepo.save(customer);
        return CustomerResponse.builder()
                .id(customer.getId())
                .accountId(customer.getAccountId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .groupId(customer.getGroupsCustomer() != null ? customer.getGroupsCustomer().getId() : null)
                .groupId(customer.getGroupId())
                .createdAt(customer.getCreatedAt())
                .deleted(customer.getDeleted())
                .deletedAt(customer.getDeletedAt())
                .build();
    }

    @Transactional
    public Customer updateCustomer(Integer id, Customer customerUpdates) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setFullName(customerUpdates.getFullName());
        customer.setPhone(customerUpdates.getPhone());
        customer.setGroupId(customerUpdates.getGroupId());
        return customerRepo.save(customer);
    }

    @Transactional
    public void deleteCustomer(Integer id) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));

        customer.setDeleted(true);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepo.save(customer);

        Integer accountId = customer.getAccountId();

        if (accountId != null) {
            accountRepo.findById(accountId).ifPresent(account -> {
                account.setStatus(GeneralStatus.LOCKED);
                accountRepo.save(account);
            });
        }
    }

//    public Customer getCustomersByGroupId(Integer id) {
//        return customerRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Customer not found"));
//    }

    @Transactional
    public Customer updateCustomerByMaster(Integer id, Customer updates, Integer masterGroupId) {

        Customer existing = customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng này!"));

        if (!existing.getGroupId().equals(masterGroupId)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền sửa thành viên của nhóm khác!");
        }

        existing.setFullName(updates.getFullName());
        return customerRepo.save(existing);
    }

    @Transactional
    public void deleteCustomerByMaster(Integer id, Integer masterGroupId) {
        Customer existing = customerRepo.getReferenceById(id);

        if (!existing.getGroupId().equals(masterGroupId)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền xóa thành viên của nhóm khác!");
        }

        customerRepo.delete(existing);
    }
}
