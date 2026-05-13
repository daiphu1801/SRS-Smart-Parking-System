package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.request.CustomerCreateRequest;
import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.service.admin.AdminCustomerService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute Customer filter,
            @RequestParam(required = false) String groupName) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomers(PageRequest.of(page, size), filter,groupName)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerCreateRequest customer) {
        return ResponseEntity.status(201).body(ApiResponse.success("Tạo Khách hàng thành công", customerService.createCustomer(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable Integer id, @RequestBody Customer customer) {

        return ResponseEntity.ok(ApiResponse.success("Cập nhật Khách hàng thành công", customerService.updateCustomer(id, customer)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa Khách hàng thành công"));
    }
}
