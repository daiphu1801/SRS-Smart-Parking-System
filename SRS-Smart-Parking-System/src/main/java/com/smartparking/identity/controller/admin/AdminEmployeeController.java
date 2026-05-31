package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.request.EmployeeCreateRequest;
import com.smartparking.identity.dto.response.EmployeeResponse;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.service.admin.AdminEmployeeService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService employeeService;
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> listEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute Employee filter) {
        PageResponse<EmployeeResponse> data = employeeService.getEmployees(PageRequest.of(page, size),filter);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeCreateRequest request) {
        EmployeeResponse data = employeeService.createEmployee(request);
        return ResponseEntity.status(201).body(ApiResponse.success(data));
    }
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable("id") Integer id) {
        EmployeeResponse data = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // XÓA MỀM (SOFT DELETE) NHÂN VIÊN
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable("id") Integer id) {
        employeeService.deleteEmployee(id);
        // Trả về null cho data để tránh lỗi Incompatible types (Void) như lúc nãy
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
