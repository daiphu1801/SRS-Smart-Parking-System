package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.request.AccountUpdateRequest;
import com.smartparking.identity.dto.response.AccountResponse;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.service.admin.AdminAccountService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService accountService;

    @PreAuthorize("hasAuthority('ACCOUNT_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute Account filter) {
        PageResponse<AccountResponse> data = accountService.getAccounts(PageRequest.of(page, size), filter);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    @PreAuthorize("hasAuthority('ACCOUNT_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Integer id,
            @RequestBody AccountUpdateRequest request) {
        AccountResponse data = accountService.updateAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", data));
    }
}
