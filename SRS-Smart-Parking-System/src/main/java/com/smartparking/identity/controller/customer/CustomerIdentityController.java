package com.smartparking.identity.controller.customer;

import com.smartparking.shared.dto.CustomAccountPrincipal;
import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.service.admin.CustomerService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/me")
@RequiredArgsConstructor
public class CustomerIdentityController {
    private final CustomerService customerService;

    @GetMapping()
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile(@AuthenticationPrincipal CustomAccountPrincipal principal) {
        Integer customerId = principal.getCustomerId();
        if (customerId == null) {
            throw new BusinessException("Tài khoản của bạn không được gắn với hồ sơ Khách hàng nào!");
        }
        CustomerResponse data = customerService.getCustomersById(customerId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PutMapping()
    public ResponseEntity<ApiResponse<Customer>> updateMyProfile(@AuthenticationPrincipal CustomAccountPrincipal principal,
                                                                 @RequestBody Customer customer) {
        Integer customerId = principal.getCustomerId();
        if (customerId == null ) {
            throw new BusinessException("Tài khoản của bạn không được gắn với hồ sơ Khách hàng nào!");
        }
        return ResponseEntity.ok(ApiResponse.success("Cập nhật Khách hàng thành công", customerService.updateCustomer(customerId, customer)));
    }
}
