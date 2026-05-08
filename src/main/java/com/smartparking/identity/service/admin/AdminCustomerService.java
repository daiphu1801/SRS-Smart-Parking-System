package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.CustomerCreateRequest;
import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.entity.*;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.specification.CustomerSpecs;
import org.springframework.data.jpa.domain.Specification;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getCustomers(Pageable pageable, Customer filter,String groupName) {
        Specification<Customer> spec = Specification
                .where(CustomerSpecs.hasFullname(filter.getFullName()))
                .and(CustomerSpecs.hasPhone(filter.getPhone()))
                .and(CustomerSpecs.hasAddress(filter.getAddress()))
                .and(CustomerSpecs.hasGroupId(filter.getGroupId()))
                .and(CustomerSpecs.hasGroupName(groupName))
                .and(CustomerSpecs.hasAccountId(filter.getAccountId()));


        Page<Customer> page = customerRepo.findAll(spec,pageable);

        List<CustomerResponse> content = page.getContent().stream().map(customer -> CustomerResponse.builder()
                .id(customer.getId())
                .accountId(customer.getAccountId())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .groupName(customer.getGroupsCustomer() != null ? customer.getGroupsCustomer().getGroupName() : null)
                .groupId(customer.getGroupId())
                .createdAt(customer.getCreatedAt())
                .build()).collect(Collectors.toList());
        return new PageResponse<>(content, page.getTotalElements(), page.getTotalPages());
    }
@Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {

        Account pendingAccount = new Account();
    if (request.getPhone().startsWith("0")) {
        request.setPhone("84" + request.getPhone().substring(1));
        pendingAccount.setUsername(request.getPhone());
    };
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

        customerRepo.save(customer);
        return CustomerResponse.builder()
                .id(customer.getId())
                .accountId(customer.getAccountId())
                .fullName(customer.getFullName())
                .phone(request.getPhone())
                .groupId(request.getGroupId())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    public Customer updateCustomer(Integer id, Customer customerUpdates) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setFullName(customerUpdates.getFullName());
        customer.setPhone(customerUpdates.getPhone());
        customer.setGroupId(customerUpdates.getGroupId());
        return customerRepo.save(customer);
    }

    public void deleteCustomer(Integer id) {
        customerRepo.deleteById(id);
    }
}
