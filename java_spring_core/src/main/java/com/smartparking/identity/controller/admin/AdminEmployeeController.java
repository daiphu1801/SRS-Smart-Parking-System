package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.request.EmployeeCreateRequest;
import com.smartparking.identity.dto.response.EmployeeResponse;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.service.admin.AdminEmployeeService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> listEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute Employee filter) {
        PageResponse<EmployeeResponse> data = employeeService.getEmployees(PageRequest.of(page, size),filter);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeCreateRequest request) {
        EmployeeResponse data = employeeService.createEmployee(request);
        return ResponseEntity.status(201).body(ApiResponse.success(data));
    }
}
