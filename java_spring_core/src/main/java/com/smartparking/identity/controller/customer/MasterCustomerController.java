package com.smartparking.identity.controller.customer;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.identity.dto.request.CustomerCreateRequest;
import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.service.admin.AdminCustomerService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/master/customer")
@RequiredArgsConstructor
public class MasterCustomerController {
    private final AdminCustomerService customerService;

    private Integer getMasterGroupId(CustomAccountPrincipal principal) {
        if (principal.getMasterGroupIds() == null || principal.getMasterGroupIds().isEmpty()) {
            throw new AccessDeniedException("Lỗi: Bạn không có quyền Master của bất kỳ nhóm nào!");
        }
        Object idObj = principal.getMasterGroupIds().get(0);

        if (idObj instanceof Number) {
            return ((Number) idObj).intValue();
        } else {
            return Integer.parseInt(idObj.toString());
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute Customer filter,
            @RequestParam(required = false) String groupName,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        // Ép filter phải lọc theo đúng Group ID của Master này
        Integer masterGroupId = getMasterGroupId(principal);
        filter.setGroupId(masterGroupId);

        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCustomers(PageRequest.of(page, size), filter, groupName)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerCreateRequest customerRequest,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer masterGroupId = getMasterGroupId(principal);
        customerRequest.setGroupId(masterGroupId);

        return ResponseEntity.status(201).body(ApiResponse.success(
                "Tạo Khách hàng thành công",
                customerService.createCustomer(customerRequest)
        ));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Integer id,
            @RequestBody Customer customerUpdates,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer masterGroupId = getMasterGroupId(principal);

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật Khách hàng thành công",
                customerService.updateCustomerByMaster(id, customerUpdates, masterGroupId)
        ));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomAccountPrincipal principal) {

        Integer masterGroupId = getMasterGroupId(principal);

        customerService.deleteCustomerByMaster(id, masterGroupId);
        return ResponseEntity.ok(ApiResponse.success("Xóa Khách hàng thành công"));
    }
}
