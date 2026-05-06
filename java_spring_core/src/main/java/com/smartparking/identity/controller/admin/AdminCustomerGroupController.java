package com.smartparking.identity.controller.admin;

import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.service.admin.AdminCustomerGroupService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/customer-groups")
@RequiredArgsConstructor
public class AdminCustomerGroupController {

    private final AdminCustomerGroupService customerGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupsCustomer>>> listCustomerGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer profileId) {
        return ResponseEntity.ok(ApiResponse.success(customerGroupService.getCustomerGroups(PageRequest.of(page, size), search, profileId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupsCustomer>> createCustomerGroup(@RequestBody GroupsCustomer group) {
        return ResponseEntity.status(201).body(ApiResponse.success("Tạo Nhóm Khách Hàng thành công", customerGroupService.createCustomerGroup(group)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupsCustomer>> updateCustomerGroup(@PathVariable Integer id, @RequestBody GroupsCustomer group) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật Nhóm Khách Hàng thành công", customerGroupService.updateCustomerGroup(id, group)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerGroup(@PathVariable Integer id) {
        customerGroupService.deleteCustomerGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa Nhóm Khách Hàng thành công"));
    }
}
