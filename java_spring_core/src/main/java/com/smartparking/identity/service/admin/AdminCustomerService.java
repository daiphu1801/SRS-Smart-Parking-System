package com.smartparking.identity.service.admin;

import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final CustomerRepository customerRepo;

    public PageResponse<Customer> getCustomers(Pageable pageable, String search, Integer groupId) {
        Page<Customer> page = customerRepo.findAll(pageable); // Add filtering logic later
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }

    public Customer createCustomer(Customer customer) {
        return customerRepo.save(customer);
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
