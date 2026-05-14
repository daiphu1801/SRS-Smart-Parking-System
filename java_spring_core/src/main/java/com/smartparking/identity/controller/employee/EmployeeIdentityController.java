package com.smartparking.identity.controller.employee;

import com.smartparking.identity.dto.CustomAccountPrincipal;
import com.smartparking.identity.dto.response.EmployeeResponse;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.service.admin.AdminEmployeeService;
import com.smartparking.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee/me")
@RequiredArgsConstructor
public class EmployeeIdentityController {
    private final AdminEmployeeService employeeService;

    @GetMapping()
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyProfile(@AuthenticationPrincipal CustomAccountPrincipal principal) {
        Integer employeeId = principal.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Tài khoản của bạn không được gắn với hồ sơ Khách hàng nào!");
        }
        EmployeeResponse data = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PutMapping()
    public ResponseEntity<ApiResponse<Employee>> updateMyProfile(@AuthenticationPrincipal CustomAccountPrincipal principal,
                                                                 @RequestBody Employee employee) {
        Integer employeeId = principal.getEmployeeId();
        if (employeeId == null || !employeeId.equals(employee.getId())) {
            throw new RuntimeException("Tài khoản của bạn không được gắn với hồ sơ Khách hàng nào!");
        }
        return ResponseEntity.ok(ApiResponse.success("Cập nhật Khách hàng thành công", employeeService.updateEmployee(employeeId, employee)));
    }
}
